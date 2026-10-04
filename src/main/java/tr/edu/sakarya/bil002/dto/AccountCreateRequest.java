package tr.edu.sakarya.bil002.dto;

public class AccountCreateRequest {

    private String ownerName;

    private String accountType;

    private Double initialBalance;

    public AccountCreateRequest() {
    }

    public AccountCreateRequest(String ownerName, String accountType, Double initialBalance) {
        this.ownerName = ownerName;
        this.accountType = accountType;
        this.initialBalance = initialBalance;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(Double initialBalance) {
        this.initialBalance = initialBalance;
    }
}
