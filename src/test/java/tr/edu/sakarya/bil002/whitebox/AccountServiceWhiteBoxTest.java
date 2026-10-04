package tr.edu.sakarya.bil002.whitebox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tr.edu.sakarya.bil002.service.AccountService;

/**
 * Beyaz kutu testleri: kaynak kodu inceleyerek dal kapsama (branch coverage) hedefleyin.
 * Her if-else dalı için en az bir test olmalı. Kapsama raporu: mvn test → target/site/jacoco/index.html
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountServiceWhiteBoxTest {

    @Autowired
    private AccountService accountService;

    @Test
    void calculateInterest_standartHesap_dogruFaiz() {
        // TODO
    }

    @Test
    void calculateInterest_premiumHesap_dogruFaiz() {
        // TODO
    }

    @Test
    void withdraw_gunlukLimitKontrol_standart() {
        // TODO
    }

    @Test
    void withdraw_gunlukLimitKontrol_premium() {
        // TODO
    }

    // ... devamı öğrenciye bırakılır
}
