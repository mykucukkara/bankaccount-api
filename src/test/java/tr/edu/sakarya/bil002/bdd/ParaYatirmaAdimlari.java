package tr.edu.sakarya.bil002.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.service.AccountService;

/**
 * ÖRNEK adım tanımları: src/test/resources/features/ornek_senaryo.feature için yazılmıştır.
 * Kendi senaryolarınız için bu sınıfa benzer yeni sınıflar ekleyin.
 */
public class ParaYatirmaAdimlari {

    @Autowired
    private AccountService accountService;

    private Account hesap;
    private double oncekiBakiye;
    private Exception yakalananHata;

    @Given("aktif bir hesap mevcut ve bakiyesi {int} TL")
    public void aktifBirHesapMevcut(int bakiye) {
        hesap = accountService.createAccount("Örnek Müşteri", "STANDARD", (double) bakiye);
    }

    @Given("dondurulmuş bir hesap mevcut")
    public void dondurulmusBirHesapMevcut() {
        hesap = accountService.createAccount("Örnek Müşteri", "STANDARD", 250.0);
        accountService.freezeAccount(hesap.getAccountNumber());
    }

    @When("hesaba {int} TL yatırılır")
    public void hesabaParaYatirilir(int tutar) {
        accountService.deposit(hesap.getAccountNumber(), (double) tutar);
    }

    @When("hesaba {int} TL yatırılmaya çalışılır")
    public void hesabaParaYatirilmayaCalisilir(int tutar) {
        oncekiBakiye = accountService.getAccount(hesap.getAccountNumber()).getBalance();
        try {
            accountService.deposit(hesap.getAccountNumber(), (double) tutar);
        } catch (Exception e) {
            yakalananHata = e;
        }
    }

    @Then("bakiye {int} TL olmalı")
    public void bakiyeOlmali(int beklenen) {
        double gercek = accountService.getAccount(hesap.getAccountNumber()).getBalance();
        assertEquals(beklenen, gercek, 0.001);
    }

    @Then("işlem geçmişinde {word} kaydı bulunmalı")
    public void islemGecmisindeKayitBulunmali(String tip) {
        TransactionType beklenenTip = TransactionType.valueOf(tip);
        boolean varMi = accountService.getTransactionHistory(hesap.getAccountNumber()).stream()
                .anyMatch(t -> t.getType() == beklenenTip);
        assertTrue(varMi, "İşlem geçmişinde " + tip + " kaydı bulunamadı");
    }

    @Then("işlem reddedilmeli")
    public void islemReddedilmeli() {
        assertNotNull(yakalananHata, "İşlemin reddedilmesi bekleniyordu ama başarılı oldu");
    }

    @Then("bakiye değişmemeli")
    public void bakiyeDegismemeli() {
        double gercek = accountService.getAccount(hesap.getAccountNumber()).getBalance();
        assertEquals(oncekiBakiye, gercek, 0.001);
    }
}
