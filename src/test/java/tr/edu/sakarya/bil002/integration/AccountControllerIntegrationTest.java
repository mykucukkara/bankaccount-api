package tr.edu.sakarya.bil002.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Entegrasyon testleri: HTTP katmanından veritabanına kadar tüm akışı test edin.
 * Endpoint ve durum kodları için docs/API.md belgesine bakın.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAccount_bulunamayan_404Donmeli() {
        // TODO
    }

    @Test
    void createAccount_gecerliIstek_201Donmeli() {
        // TODO
    }

    @Test
    void deposit_aktifHesap_200Donmeli() {
        // TODO
    }

    // ... devamı öğrenciye bırakılır
}
