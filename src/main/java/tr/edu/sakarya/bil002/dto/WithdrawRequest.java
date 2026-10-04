package tr.edu.sakarya.bil002.dto;

import jakarta.validation.constraints.NotNull;

public class WithdrawRequest {

    @NotNull(message = "Tutar (amount) zorunludur")
    private Double amount;

    private String description;

    public WithdrawRequest() {
    }

    public WithdrawRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
