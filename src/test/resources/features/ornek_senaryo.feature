# Bu dosya örnek olarak verilmiştir. Öğrenciler kendi feature dosyalarını yazacak.
# Adım tanımları: src/test/java/tr/edu/sakarya/bil002/bdd/ParaYatirmaAdimlari.java

Feature: Para Yatırma İşlemi
  Müşteri banka hesabına para yatırmak istiyor

  Scenario: Geçerli miktarda para yatırma
    Given aktif bir hesap mevcut ve bakiyesi 1000 TL
    When hesaba 500 TL yatırılır
    Then bakiye 1500 TL olmalı
    And işlem geçmişinde DEPOSIT kaydı bulunmalı

  Scenario: Dondurulmuş hesaba para yatırma girişimi
    Given dondurulmuş bir hesap mevcut
    When hesaba 100 TL yatırılmaya çalışılır
    Then işlem reddedilmeli
    And bakiye değişmemeli
