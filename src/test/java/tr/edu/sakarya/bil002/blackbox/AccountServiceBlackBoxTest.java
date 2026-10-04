package tr.edu.sakarya.bil002.blackbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tr.edu.sakarya.bil002.service.AccountService;

/**
 * Kara kutu testleri: yalnızca docs/GEREKSINIMLER.md belgesine bakarak yazılmalıdır.
 * Kaynak kodu okumadan, girdi/çıktı davranışını test edin.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountServiceBlackBoxTest {

    @Autowired
    private AccountService accountService;

    // ===================== EP TESTLERİ — Denklik Sınıfı Bölümleme =====================

    @Test
    void deposit_gecerliMiktar_basarili() {
        // TODO
    }

    @Test
    void deposit_sifirMiktar_reddedilmeli() {
        // TODO
    }

    @Test
    void deposit_negatifMiktar_reddedilmeli() {
        // TODO
    }

    @Test
    void deposit_dondurulmusHesap_reddedilmeli() {
        // TODO
    }

    @Test
    void deposit_kapaliHesap_reddedilmeli() {
        // TODO
    }

    // ===================== BVA TESTLERİ — Sınır Değer Analizi =====================

    @Test
    void deposit_sifirTL_sinirDegeri() {
        // TODO
    }

    @Test
    void deposit_birKurus_minimumGecerli() {
        // TODO
    }

    @Test
    void deposit_standartMaksimum_10000TL() {
        // TODO
    }

    @Test
    void deposit_standartMaksimumuAsan_10001TL() {
        // TODO
    }

    @Test
    void withdraw_tamBakiye_cekilebilmeli() {
        // TODO
    }

    @Test
    void withdraw_bakiyedenFazla_reddedilmeli() {
        // TODO
    }

    // ===================== DURUM GEÇİŞ TESTLERİ =====================

    @Test
    void hesap_aktiftenDonduruldu() {
        // TODO
    }

    @Test
    void hesap_donduruldutanAktife() {
        // TODO
    }

    @Test
    void hesap_aktiftenKapali() {
        // TODO
    }

    @Test
    void hesap_kapaliHesapYenidenAcilamaz() {
        // TODO
    }

    @Test
    void hesap_dondurulmusHesapKapatma_bakiyeSifirsa() {
        // TODO
    }

    // ===================== KARAR TABLOSU — Transfer kuralları =====================

    @Test
    void transfer_ikisiDeAktif_basarili() {
        // TODO
    }

    @Test
    void transfer_kaynakDondurulmus_reddedilmeli() {
        // TODO
    }

    @Test
    void transfer_hedefDondurulmus_reddedilmeli() {
        // TODO
    }

    @Test
    void transfer_ayniHesap_reddedilmeli() {
        // TODO
    }
}
