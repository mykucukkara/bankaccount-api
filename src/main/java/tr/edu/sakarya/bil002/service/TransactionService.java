package tr.edu.sakarya.bil002.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.repository.TransactionRepository;

@Service
public class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction record(Account account, TransactionType type, double amount, String description) {
        Transaction transaction = new Transaction(account.getAccountNumber(), type, amount, account.getBalance(),
                description, TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);
        LOG.info("İşlem kaydedildi: hesap={}, tip={}, tutar={}", account.getAccountNumber(), type, amount);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getHistory(String accountNumber) {
        return transactionRepository.findByAccountNumberOrderByTimestampDescIdDesc(accountNumber);
    }

    @Transactional(readOnly = true)
    public double getDailyWithdrawalTotal(String accountNumber) {
        return transactionRepository
                .findByAccountNumberAndTypeAndStatus(accountNumber, TransactionType.WITHDRAWAL,
                        TransactionStatus.SUCCESS)
                .stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
