package tr.edu.sakarya.bil002.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransferRequest {

    @NotBlank(message = "Gönderen hesap numarası (fromAccount) zorunludur")
    private String fromAccount;

    @NotBlank(message = "Alıcı hesap numarası (toAccount) zorunludur")
    private String toAccount;

    @NotNull(message = "Tutar (amount) zorunludur")
    private Double amount;

    public TransferRequest() {
    }

    public TransferRequest(String fromAccount, String toAccount, Double amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
