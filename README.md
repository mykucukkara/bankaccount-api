# BankAccount API — BIL 002 Yazılım Doğrulama ve Sınama Dönem Projesi

BankAccount API, hesap açma, para yatırma/çekme, hesaplar arası transfer, hesap dondurma/kapatma
ve aylık faiz hesaplama işlemlerini sunan basit bir banka hesap yönetim REST servisidir. Uygulama
Spring Boot 3 ve H2 bellek içi veritabanı ile geliştirilmiştir. **Uygulamada bilerek bırakılmış
hatalar bulunmaktadır.** Bu dönem boyunca göreviniz; statik analiz, kara kutu, beyaz kutu, TDD, BDD,
entegrasyon ve performans testi tekniklerini kullanarak bu uygulamayı sistematik biçimde test etmek,
hataları bulmak ve bulgularınızı raporlamaktır. Doğru davranışın tek referansı
[docs/GEREKSINIMLER.md](docs/GEREKSINIMLER.md) belgesidir.

> 📘 **Başlamadan önce [docs/ODEV_REHBERI.md](docs/ODEV_REHBERI.md) belgesini okuyun.** Ödev
> isterleri, son teslim tarihi (**20 Aralık 2026 Pazar**), dosyaların nereye konacağı, teslim
> kuralları ve hatayı yakalayan testlerin nasıl etiketleneceği orada anlatılıyor.

---

## Gereksinimler

- **Java 17** (JDK — ör. [Eclipse Temurin](https://adoptium.net/))
- **Maven 3.8+**
- Git, bir IDE (IntelliJ IDEA / VS Code / Eclipse)
- İsteğe bağlı: Postman, Apache JMeter, SonarQube / SonarLint

Kurulumu kontrol edin:

```bash
java -version
mvn -version
```

## Nasıl Çalıştırılır

```bash
mvn spring-boot:run
```

Uygulama `http://localhost:8080` adresinde ayağa kalkar. Veritabanı bellek içidir; uygulama her
yeniden başlatıldığında veriler sıfırlanır.

Diğer faydalı komutlar:

| Komut | Açıklama |
|-------|----------|
| `mvn test` | Tüm testleri çalıştırır + JaCoCo raporu üretir (`target/site/jacoco/index.html`) |
| `mvn checkstyle:check` | Statik kod stili kontrolü |
| `mvn verify` | Derleme + test + JaCoCo raporu (kapsama eşiği tanımlı değildir, düşük kapsama build'i bozmaz) |
| `mvn test -Dtest=AccountServiceBlackBoxTest` | Tek bir test sınıfını çalıştırır |
| `mvn test -DexcludedGroups=hata` | `hata` etiketli testler hariç hepsini çalıştırır (CI'daki zorunlu adım) |
| `mvn test -Dgroups=hata` | Yalnızca hatayı yakalayan (`hata` etiketli) testleri çalıştırır |

> **Windows uyarısı:** Projeyi yolunda **Türkçe karakter** (ğ, ş, ı, ö, ü, ç) bulunan bir klasöre
> koymayın (ör. `C:\Users\...\Masaüstü\Ödevler\...`). Bu durumda JaCoCo ajanı kapsama verisini
> yazamaz ve `target/site/jacoco` raporu **oluşmaz** ("Skipping JaCoCo execution due to missing
> execution data file"). `C:\projeler\bankaccount-api` gibi bir yol kullanın.

## H2 Veritabanı Konsolu

Uygulama çalışırken tarayıcıdan açın: **http://localhost:8080/h2-console**

| Alan | Değer |
|------|-------|
| JDBC URL | `jdbc:h2:mem:bankdb` |
| User Name | `sa` |
| Password | *(boş)* |

Tablolar: `ACCOUNTS`, `TRANSACTIONS`.

## API Endpoint'leri (Özet)

| Metot | Yol | Açıklama |
|-------|-----|----------|
| POST | `/api/accounts` | Hesap aç |
| GET | `/api/accounts` | Tüm hesaplar |
| GET | `/api/accounts/{no}` | Hesap bilgisi |
| POST | `/api/accounts/{no}/deposit` | Para yatır |
| POST | `/api/accounts/{no}/withdraw` | Para çek |
| POST | `/api/accounts/{no}/freeze` | Dondur |
| POST | `/api/accounts/{no}/unfreeze` | Dondurmayı kaldır |
| POST | `/api/accounts/{no}/close` | Kapat |
| POST | `/api/accounts/{no}/interest` | Aylık faiz uygula |
| GET | `/api/accounts/{no}/transactions` | İşlem geçmişi |
| POST | `/api/transactions/transfer` | Transfer |

İstek/yanıt örnekleri ve durum kodları için: [docs/API.md](docs/API.md)

## Proje Yapısı

```
bankaccount-api/
├── src/main/java/tr/edu/sakarya/bil002/
│   ├── controller/   REST uç noktaları
│   ├── service/      İş kuralları (AccountService, TransactionService)
│   ├── model/        JPA varlıkları ve enum'lar
│   ├── repository/   Spring Data JPA depoları
│   ├── dto/          İstek/yanıt nesneleri
│   └── exception/    Özel istisnalar ve global hata yakalayıcı
├── src/test/java/tr/edu/sakarya/bil002/
│   ├── blackbox/     Kara kutu test iskeleti
│   ├── whitebox/     Beyaz kutu test iskeleti
│   ├── integration/  Entegrasyon test iskeleti
│   └── bdd/          Cucumber koşucusu + örnek adım tanımları
├── src/test/resources/features/   Çalıştırılan Cucumber feature dosyaları
├── features/                      Örnek Gherkin senaryosu (referans)
└── docs/                          Gereksinimler, API, rapor şablonu
```

## Test İskeletleri

| Dosya | Teknik | Açıklama |
|-------|--------|----------|
| [blackbox/AccountServiceBlackBoxTest.java](src/test/java/tr/edu/sakarya/bil002/blackbox/AccountServiceBlackBoxTest.java) | EP, BVA, karar tablosu, durum geçişi | Gövdeleri `// TODO` olan 20 test metodu |
| [whitebox/AccountServiceWhiteBoxTest.java](src/test/java/tr/edu/sakarya/bil002/whitebox/AccountServiceWhiteBoxTest.java) | Dal kapsama | Başlangıç metotları; geri kalanını siz ekleyeceksiniz |
| [integration/AccountControllerIntegrationTest.java](src/test/java/tr/edu/sakarya/bil002/integration/AccountControllerIntegrationTest.java) | MockMvc ile HTTP testleri | Başlangıç metotları |
| [bdd/CucumberRunnerTest.java](src/test/java/tr/edu/sakarya/bil002/bdd/CucumberRunnerTest.java) | BDD / Cucumber | Koşucu; örnek adımlar `ParaYatirmaAdimlari.java` içinde |

> **Not:** Boş test metotları "geçti" olarak görünür. Bir testin anlamlı olması için en az bir
> doğrulama (`assertEquals`, `assertThrows`, `andExpect(...)` vb.) içermesi gerekir.
> Kasıtlı hataları yakalayan testleriniz **kırmızı (başarısız)** olacaktır — bu beklenen bir
> durumdur. Uygulama kodunu düzeltmeyin; bu testleri **`@Tag("hata")`** (Cucumber'da **`@hata`**)
> ile etiketleyin ki CI yeşil kalsın, hatayı da raporlayın. Ayrıntılar:
> [ODEV_REHBERI.md §4](docs/ODEV_REHBERI.md#4-hatayı-yakalayan-testler-hata-etiketi)

BDD senaryoları `src/test/resources/features/` altından çalıştırılır. Adım tanımlarını
`tr.edu.sakarya.bil002.bdd` paketine ekleyin. `features/ornek_senaryo.feature` yalnızca
referans içindir.

## Rapor

Rapor şablonu: [docs/RAPOR_SABLONU.md](docs/RAPOR_SABLONU.md). Şablonu kopyalayıp doldurun ve
reponuzda `docs/RAPOR.md` olarak teslim edin. Teslim edilecek dosyaların tam listesi:
[docs/ODEV_REHBERI.md](docs/ODEV_REHBERI.md)

## Haftalık Görevler

| Hafta | Konu | Görev / Teslim |
|-------|------|----------------|
| 1 | Başlangıç | **Use this template** ile kendi public reponuzu oluşturun (Issues açık), klonlayın, uygulamayı çalıştırın, H2 konsoluna ve API'ye erişin. `ODEV_REHBERI.md` ve `GEREKSINIMLER.md`'yi okuyun. |
| 2 | Statik test | `mvn checkstyle:check` ve SonarQube/SonarLint çalıştırın; kod incelemesi yapın. Rapor Bölüm 1. |
| 3 | Denklik sınıfı bölümleme | `deposit`, `withdraw`, `createAccount` için EP tabloları ve testleri. |
| 4 | Sınır değer analizi | Tutar ve limit sınırları için BVA testleri (0, 0,01, limit, limit+0,01 ...). |
| 5 | Karar tablosu | Transfer ve limit kuralları için karar tablosu + testler. |
| 6 | Durum geçiş testi | ACTIVE / FROZEN / CLOSED geçişlerinin tamamı için testler. Rapor Bölüm 2. |
| 7 | Beyaz kutu | JaCoCo raporunu inceleyin, kapsanmayan dallar için test ekleyin. Hedef: `service` paketinde ≥ %80 dal kapsama. `jacoco.csv`'yi `docs/kapsama/` altına commit edin. Rapor Bölüm 3. |
| 8 | TDD | **Yeni bir özelliği** kırmızı-yeşil-yeniden düzenleme döngüsüyle geliştirin (her adım ayrı commit). Rapor Bölüm 4. |
| 9 | BDD | Örnek hariç **en az 5** feature dosyası yazın ve adımlarını tanımlayın. Rapor Bölüm 5. |
| 10 | Entegrasyon / API | `AccountControllerIntegrationTest`'i tamamlayın; Postman koleksiyonunu `docs/postman/` altına ekleyin. Rapor Bölüm 6. |
| 11 | Performans | JMeter ile yük testi (ör. 50 eşzamanlı kullanıcı para yatırma/transfer); `.jmx` ve sonuçları `performans/` altına ekleyin. Rapor Bölüm 7. |
| 12 | CI/CD | Son commit'te pipeline'ın yeşil olduğundan ve `@hata` özetinin göründüğünden emin olun. Rapor Bölüm 8. |
| 13 | Rapor | Bulunan tüm hataları listeleyin, raporu tamamlayın ve teslim edin. Rapor Bölüm 9–10. |

## Sürekli Entegrasyon (GitHub Actions)

[.github/workflows/ci.yml](.github/workflows/ci.yml) içindeki **BIL002 CI Pipeline**,
`main` ve `develop` dallarına yapılan her **push**'ta ve bu dallara açılan her **pull request**'te
otomatik çalışır. Sırasıyla: Checkstyle → derleme → testler (`hata` etiketliler hariç, geçmesi
zorunlu) → `hata` etiketli testler (kırmızı olması beklenir, pipeline'ı bozmaz; özeti çalışmanın
**Summary** sayfasında) → JaCoCo raporu. Testler başarısız olsa bile JaCoCo raporu, test sonuçları
ve Checkstyle raporu **Actions → ilgili çalışma → Artifacts** bölümünden indirilebilir.
