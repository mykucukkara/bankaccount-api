package tr.edu.sakarya.bil002.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.edu.sakarya.bil002.exception.AccountNotFoundException;
import tr.edu.sakarya.bil002.exception.InsufficientBalanceException;
import tr.edu.sakarya.bil002.exception.InvalidAccountStateException;
import tr.edu.sakarya.bil002.exception.LimitExceededException;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.AccountStatus;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.repository.AccountRepository;

@Service
public class AccountService {

    public static final String STANDARD = "STANDARD";
    public static final String PREMIUM = "PREMIUM";

    private static final double STANDARD_MAX_DEPOSIT = 10_000.0;
    private static final double PREMIUM_MAX_DEPOSIT = 50_000.0;
    private static final double STANDARD_DAILY_WITHDRAWAL_LIMIT = 5_000.0;
    private static final double PREMIUM_DAILY_WITHDRAWAL_LIMIT = 20_000.0;

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final Random random = new Random();

    public AccountService(AccountRepository accountRepository, TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Account createAccount(String ownerName, String accountType, Double initialBalance) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Hesap sahibi adı boş olamaz");
        }
        if (!STANDARD.equals(accountType) && !PREMIUM.equals(accountType)) {
            throw new IllegalArgumentException("Hesap türü STANDARD veya PREMIUM olmalıdır");
        }
        double balance = initialBalance != null ? initialBalance : 0.0;

        Account account = new Account(generateAccountNumber(), ownerName.trim(), accountType, balance);
        Account saved = accountRepository.save(account);
        LOG.info("Hesap oluşturuldu: {} ({})", saved.getAccountNumber(), accountType);
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Account> findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account deposit(String accountNumber, Double amount) {
        Account account = getAccount(accountNumber);
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Yatırılacak tutar sıfırdan büyük olmalıdır");
        }
        requireActive(account);
        checkDepositLimit(account, amount);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        transactionService.record(account, TransactionType.DEPOSIT, amount, "Para yatırma");
        LOG.info("Para yatırıldı: hesap={}, tutar={}", accountNumber, amount);
        return account;
    }

    @Transactional
    public Account withdraw(String accountNumber, Double amount) {
        Account account = getAccount(accountNumber);
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Çekilecek tutar sıfırdan büyük olmalıdır");
        }
        ensureSufficientBalance(account, amount);
        checkDailyWithdrawalLimit(account, amount);

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        transactionService.record(account, TransactionType.WITHDRAWAL, amount, "Para çekme");
        LOG.info("Para çekildi: hesap={}, tutar={}", accountNumber, amount);
        return account;
    }

    @Transactional
    public void transfer(String fromAccount, String toAccount, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Transfer tutarı sıfırdan büyük olmalıdır");
        }
        Account source = getAccount(fromAccount);
        Account target = getAccount(toAccount);
        validateTransferParties(source, target);

        if (source.getBalance() < amount) {
            throw new InsufficientBalanceException("Gönderen hesabın bakiyesi yetersiz");
        }

        source.setBalance(source.getBalance() - amount);
        accountRepository.save(source);
        transactionService.record(source, TransactionType.TRANSFER, amount, "Giden transfer -> " + toAccount);

        target.setBalance(target.getBalance() + amount);
        accountRepository.save(target);
        transactionService.record(target, TransactionType.TRANSFER, amount, "Gelen transfer <- " + fromAccount);

        LOG.info("Transfer tamamlandı: {} -> {}, tutar={}", fromAccount, toAccount, amount);
    }

    @Transactional
    public Account freezeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountStateException("Kapalı hesap dondurulamaz");
        }
        account.setStatus(AccountStatus.FROZEN);
        LOG.info("Hesap donduruldu: {}", accountNumber);
        return accountRepository.save(account);
    }

    @Transactional
    public Account unfreezeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new InvalidAccountStateException("Sadece dondurulmuş hesaplar aktifleştirilebilir");
        }
        account.setStatus(AccountStatus.ACTIVE);
        LOG.info("Hesap aktifleştirildi: {}", accountNumber);
        return accountRepository.save(account);
    }

    @Transactional
    public Account closeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountStateException("Hesap zaten kapalı");
        }
        account.setStatus(AccountStatus.CLOSED);
        LOG.info("Hesap kapatıldı: {}", accountNumber);
        return accountRepository.save(account);
    }

    @Transactional
    public Account calculateInterest(String accountNumber) {
        Account account = getAccount(accountNumber);
        requireActive(account);

        int annualRate = PREMIUM.equals(account.getAccountType()) ? 5 : 3;
        double interest = account.getBalance().longValue() * annualRate / 100 / 12;

        account.setBalance(account.getBalance() + interest);
        accountRepository.save(account);
        transactionService.record(account, TransactionType.INTEREST, interest, "Aylık faiz (%" + annualRate + ")");
        LOG.info("Faiz uygulandı: hesap={}, faiz={}", accountNumber, interest);
        return account;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(String accountNumber) {
        getAccount(accountNumber);
        return transactionService.getHistory(accountNumber);
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException(
                    "Hesap aktif değil: " + account.getAccountNumber() + " (" + account.getStatus() + ")");
        }
    }

    private void validateTransferParties(Account source, Account target) {
        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException("Gönderen hesap aktif değil: " + source.getAccountNumber());
        }
    }

    private void ensureSufficientBalance(Account account, double amount) {
        if (amount >= account.getBalance()) {
            throw new InsufficientBalanceException("Yetersiz bakiye");
        }
    }

    private void checkDepositLimit(Account account, double amount) {
        double limit = PREMIUM.equals(account.getAccountType()) ? PREMIUM_MAX_DEPOSIT : STANDARD_MAX_DEPOSIT;
        if (amount > limit) {
            throw new LimitExceededException("Tek seferlik para yatırma limiti aşıldı: " + limit + " TL");
        }
    }

    private void checkDailyWithdrawalLimit(Account account, double amount) {
        double withdrawnToday = transactionService.getDailyWithdrawalTotal(account.getAccountNumber());
        double total = withdrawnToday + amount;
        if (PREMIUM.equals(account.getAccountType())) {
            if (total >= PREMIUM_DAILY_WITHDRAWAL_LIMIT) {
                throw new LimitExceededException("Günlük çekim limiti aşıldı: " + PREMIUM_DAILY_WITHDRAWAL_LIMIT
                        + " TL");
            }
        } else if (total > STANDARD_DAILY_WITHDRAWAL_LIMIT) {
            throw new LimitExceededException("Günlük çekim limiti aşıldı: " + STANDARD_DAILY_WITHDRAWAL_LIMIT
                    + " TL");
        }
    }

    private String generateAccountNumber() {
        long number = 1_000_000_000L + random.nextLong(9_000_000_000L);
        return String.valueOf(number);
    }
}
