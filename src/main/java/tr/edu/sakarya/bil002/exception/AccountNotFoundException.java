package tr.edu.sakarya.bil002.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountNumber) {
        super("Hesap bulunamadı: " + accountNumber);
    }
}
