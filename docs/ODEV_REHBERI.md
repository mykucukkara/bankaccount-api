# BIL 002 Dönem Projesi — Öğrenci Rehberi

Sevgili öğrenciler,

Bu belgede dönem projesinde sizden **ne beklediğimi, hangi dosyaları nereye koymanız gerektiğini ve
çalışmanızı nasıl değerlendireceğimi** anlattım. Lütfen başlamadan önce baştan sona okuyun.

> **Son teslim tarihi: 20 Aralık 2026 Pazar**
> Değerlendirmede reponuzun bu tarihteki **son commit'ini** esas alacağım.

---

## 1. Projenin Amacı

Size, içine **bilerek hatalar yerleştirdiğim** bir banka hesap yönetim API'si veriyorum. Sizden
kodu düzeltmenizi değil, **derste gördüğümüz test tekniklerini uygulayarak bu hataları bulmanızı,
kanıtlamanızı ve raporlamanızı** bekliyorum.

- Uygulamanın nasıl davranması gerektiğini anlatan tek referans: [GEREKSINIMLER.md](GEREKSINIMLER.md)
- Uygulama bu belgeden farklı davranıyorsa **hata** vardır.
- Uygulama belgeye uygun davranıyorsa, size garip gelse bile **hata değildir**. Bir davranışı hata
  olarak raporlamadan önce belgede hangi kurala aykırı olduğunu (örn. `R-3.4`) bulun. Gereksinime
  uygun bir davranışı hata olarak raporlarsanız bunu bulgu olarak kabul etmeyeceğim.

---

## 2. Başlangıç

1. Şablon reposunun GitHub sayfasında **Use this template → Create a new repository** düğmesine tıklayın.
   - **Owner:** kendi GitHub hesabınız
   - **Repository name:** serbest (örn. `bil002-bankaccount-<ogrenci-no>`)
   - **Visibility:** **Public** *(zorunlu, reponuzu inceleyebilmem için herkese açık olmalı)*
2. Oluşan reponuzda **Settings → General → Features → Issues** seçeneğinin **açık** olduğundan emin
   olun. Değerlendirme sonucunu reponuza **Issue** olarak göndereceğim.
3. Repo URL'nizi **bana göndermeniz beklenmektedir.** Nasıl teslim edeceğinizi daha sonra
   duyuracağım.
4. Repoyu bilgisayarınıza klonlayın ve çalıştırın (bkz. [README](../README.md)):

   ```bash
   git clone https://github.com/<kullanici>/<repo>.git
   cd <repo>
   mvn spring-boot:run
   ```

> **Windows:** Repoyu yolunda Türkçe karakter (ğ, ş, ı, ö, ü, ç) olan bir klasöre klonlamayın.
> Aksi halde JaCoCo kapsama raporu oluşmaz. `C:\projeler\...` gibi bir yol kullanın.

---

## 3. Temel Kurallar

| # | Kural |
|---|-------|
| 1 | `src/main/` altındaki **mevcut kodu değiştirmeyin**, kasıtlı hataları **düzeltmeyin**. Hataları testlerinizle kanıtlayın. *(İstisna: TDD bölümünde **yeni bir özellik** için kod ekleyebilirsiniz, bkz. Bölüm 5.3.)* |
| 2 | `docs/GEREKSINIMLER.md` dosyasını değiştirmeyin. |
| 3 | Kasıtlı bir hatayı yakalayan (yani **başarısız olan**) her testi `hata` etiketiyle işaretleyin (bkz. Bölüm 4). |
| 4 | Boş veya doğrulama (assert) içermeyen testleri saymayacağım. Her test en az bir doğrulama içermelidir. |
| 5 | `.github/workflows/ci.yml` içindeki adımları silmeyin veya devre dışı bırakmayın. Yeni adım ekleyebilirsiniz. |
| 6 | Düzenli ve anlamlı commit'ler atın. Çalışmanın gelişimini commit geçmişinden takip edeceğim; tüm çalışmanın tek bir commit'te gelmesini istemiyorum. |
| 7 | Rapor ve testler size ait olmalıdır. Başka bir öğrencinin reposundan kopyalanan içeriği her iki taraf için de kopya olarak değerlendireceğim. |

---

## 4. Hatayı Yakalayan Testler: `hata` Etiketi

Testiniz kasıtlı bir hatayı yakaladığında **kırmızı** olur; bu sizin açınızdan başarıdır. Öte yandan
CI pipeline'ınızın **yeşil** kalmasını da bekliyorum. İkisini birlikte sağlamak için hatayı yakalayan
testleri etiketleyin:

**JUnit:**

```java
import org.junit.jupiter.api.Tag;

@Tag("hata")
@Test
void ornek_kuralIhlali_reddedilmeli() {
    // R-x.y: <gereksinimdeki kural>. Rapor: H-xx
    Account hesap = accountService.createAccount("Ali", "STANDARD", 100.0);
    assertThrows(<BeklenenIstisna>.class,
            () -> accountService.<islem>(hesap.getAccountNumber(), <deger>));
}
```

**Cucumber (Gherkin):**

```gherkin
@hata
Scenario: <Kural ihlali> reddedilmeli
  Given <başlangıç durumu>
  When <kuralı ihlal eden işlem> yapılmaya çalışılır
  Then işlem reddedilmeli
```

Yukarıdaki örnekler yalnızca **yapıyı** gösteriyor; `<...>` ile işaretli yerleri kendi bulgunuza göre
doldurun. Etiketi test metoduna ya da senaryoya ekliyorsunuz, sınıfın veya feature dosyasının
tamamına değil.

**CI'da ne olur?**

| Adım | Çalışan testler | Sonuç |
|------|-----------------|-------|
| *Testleri çalıştır* | `hata` etiketi **olmayan** tüm testler | Geçmek **zorundadır**. Biri bile kırmızıysa pipeline kırmızı olur. |
| *Bulunan hata testlerini çalıştır (@hata)* | Yalnızca `hata` etiketli testler | Kırmızı olması **beklenir**, pipeline'ı kırmızıya çevirmez. Kaç hata yakaladığınız Actions çalışmasının **Summary** sayfasında görünür. |

Kapsama (JaCoCo) raporu her iki gruptaki testleri birlikte sayar.

**Yerelde çalıştırma:**

```bash
mvn test -DexcludedGroups=hata   # CI'daki zorunlu adımın aynısı, yeşil olmalı
mvn test -Dgroups=hata           # yalnızca hatayı yakalayan testler, kırmızı olması beklenir
mvn test                         # hepsi
```

> Etiketsiz bir test kırmızıysa iki ihtimal vardır: ya yeni bir hata buldunuz (`@Tag("hata")` ekleyin
> ve raporlayın) ya da testinizin kendisi yanlıştır (gereksinimi tekrar okuyun).

---

## 5. Teslim Edilecekler

Çalışmanızı aşağıdaki başlıklar altında değerlendireceğim. Dosyaları **tabloda belirtilen konumlara**
koyun; incelerken bu konumlara bakacağım.

| # | Başlık | Nereye? |
|---|--------|---------|
| 1 | Kara Kutu Test Teknikleri | `src/test/java/.../blackbox/` + Rapor Bölüm 2 |
| 2 | Beyaz Kutu Test ve Kod Kapsamı | `src/test/java/.../whitebox/` + `docs/kapsama/jacoco.csv` + Rapor Bölüm 3 |
| 3 | TDD Uygulaması | Commit geçmişi + Rapor Bölüm 4 |
| 4 | BDD / Cucumber Senaryoları | `src/test/resources/features/*.feature` + `src/test/java/.../bdd/` |
| 5 | API / Entegrasyon Testi | `src/test/java/.../integration/` + `docs/postman/*.postman_collection.json` |
| 6 | Performans Testi | `performans/*.jmx` + `performans/sonuclar/` + Rapor Bölüm 7 |
| 7 | CI/CD Pipeline | `.github/workflows/ci.yml`, son commit'te yeşil |
| 8 | Rapor | `docs/RAPOR.md` |

### 5.1 Kara Kutu Test Teknikleri

Kaynak kodu okumadan, **yalnızca GEREKSINIMLER.md'ye bakarak** test tasarlamanızı istiyorum.

- **En az 20 farklı** test senaryosu (iskeletteki 20 metot bir başlangıçtır, içlerini doldurmanız gerekir)
- **Denklik Sınıfı Bölümleme (EP):** geçerli ve geçersiz sınıflar (tutar, hesap türü, hesap durumu ...)
- **Sınır Değer Analizi (BVA):** sınırın altı, sınır, üstü (`0` / `0,01`, `10.000` / `10.000,01`,
  tam bakiye, günlük limit ...)
- **Karar tablosu:** transfer kuralları (kaynak/hedef durumu × aynı hesap × bakiye yeterliliği) ve
  limit kuralları (hesap türü × tutar)
- **Durum geçiş testi:** [GEREKSINIMLER.md §4.2](GEREKSINIMLER.md#42-durum-geçiş-tablosu)
  tablosundaki **tüm** satırlar (geçerli ve geçersiz geçişler)
- Raporda her teknik için tabloyu doldurun. Test ID ile test metodu adı arasındaki eşleşme görünmeli.

### 5.2 Beyaz Kutu Test ve Kod Kapsamı

- `AccountService` ve `TransactionService`'in kaynak kodunu inceleyin; her `if`/`else` dalı ve her
  koşul için test yazın.
- **Hedef: `service` paketinde dal kapsamı (branch coverage) ≥ %80.**
- Kapsama raporu `mvn test` sonrasında `target/site/jacoco/index.html` konumunda oluşur.
- **Teslim:** `target/site/jacoco/jacoco.csv` dosyasını `docs/kapsama/jacoco.csv` olarak repoya
  **commit edin**, ekran görüntüsünü de `docs/img/` altına koyun. (`target/` klasörü git'e gitmez,
  bu yüzden dosyayı kopyalamanız gerekir.)
- Raporda kapsanamayan dalları ve nedenlerini açıklayın.

### 5.3 TDD Uygulaması

Uygulamaya **yeni bir özelliği** Kırmızı → Yeşil → Yeniden Düzenleme döngüsüyle eklemenizi
istiyorum. Mevcut kasıtlı hataları düzeltmeyi TDD olarak kabul etmeyeceğim. Önerdiğim özellikler
şunlar; birini seçebilir ya da kendi önerinizi bana danışabilirsiniz:

- `GET /api/accounts/{no}/summary`: toplam yatırılan, toplam çekilen ve işlem sayısı
- Hesap sahibine göre arama: `GET /api/accounts?owner=Ayşe`
- Transfer için tek seferlik üst limit (STANDARD 25.000 TL, PREMIUM 100.000 TL)
- Hesap türü yükseltme: STANDARD → PREMIUM (yalnızca ACTIVE hesap, bakiye ≥ 1.000 TL)

**Döngünün commit geçmişinde görünmesini bekliyorum.** Her adım için ayrı commit atın:

```
test(red): hesap özeti için başarısız test
feat(green): hesap özeti endpoint'i
refactor: özet hesaplamasını TransactionService'e taşı
```

Raporda ilgili commit'lerin hash'lerini ve kırmızı testin hata çıktısını gösterin.

### 5.4 BDD / Cucumber Senaryoları

- `src/test/resources/features/` altında, örnek dosya (`ornek_senaryo.feature`) **hariç en az 5**
  `.feature` dosyası
- Her dosya tek bir işleve odaklansın (para çekme, transfer, hesap durumu, faiz ...). Her dosyada
  birden fazla senaryo olsun; uygun yerlerde `Scenario Outline` + `Examples` kullanın.
- Adım tanımlarını `tr.edu.sakarya.bil002.bdd` paketine ekleyin (örnek:
  [ParaYatirmaAdimlari.java](../src/test/java/tr/edu/sakarya/bil002/bdd/ParaYatirmaAdimlari.java)).
  Aynı adım cümlesini iki sınıfta tanımlamayın.
- Senaryolar iş diliyle yazılmalı ("hesaba 500 TL yatırılır"), kod ayrıntısı içermemeli.
- Kök dizindeki `features/` klasörü **çalıştırılmaz**, yalnızca referans içindir.

### 5.5 API / Entegrasyon Testi

- `AccountControllerIntegrationTest` dosyasını tamamlayın. `MockMvc` ile HTTP durum kodlarını ve
  JSON gövdesini doğrulayın. Gerekirse `TransactionController` için ayrı bir sınıf ekleyin.
- Her endpoint için en az bir başarılı ve bir hatalı istek testi yazın. Beklenen kodları
  [API.md](API.md) ve [GEREKSINIMLER.md §7](GEREKSINIMLER.md#7-hata-durumlarında-beklenen-davranış)
  belgelerinde bulabilirsiniz.
- **Postman:** Koleksiyonu dışa aktarıp `docs/postman/bankaccount.postman_collection.json` olarak
  commit edin. İsteklerde `pm.test(...)` ile doğrulama bulunsun; hesap numarasını ortam değişkeniyle
  istekler arasında taşıyın.

### 5.6 Performans Testi

- Apache JMeter ile en az bir yük testi senaryosu hazırlayın, örneğin:
  - 50 eşzamanlı kullanıcı, 60 saniye boyunca para yatırma + bakiye sorgulama
  - Aynı iki hesap arasında eşzamanlı transfer (bakiye tutarlılığını da kontrol edin)
- Test planını `performans/bankaccount-yuk-testi.jmx` olarak commit edin.
- Sonuçları (`.csv` / HTML rapor ekran görüntüleri) `performans/sonuclar/` altına koyun.
- Raporda ortalama ve 90. persentil yanıt süresini, saniyedeki istek sayısını ve hata yüzdesini
  **yorumlayın**: Darboğaz var mı, eşzamanlılık bir hataya yol açtı mı?

### 5.7 CI/CD Pipeline

- Pipeline `main` ve `develop` dallarına yapılan her push'ta çalışır.
- **Son commit'inizde pipeline'ın yeşil olmasını bekliyorum.** Bunun için hatayı yakalayan testleri
  `hata` ile etiketleyin (Bölüm 4).
- Checkstyle hatası da pipeline'ı kırmızıya çevirir. Push etmeden önce `mvn checkstyle:check`
  çalıştırın.
- İsteğe bağlı (iyi uygulama): README'ye pipeline durum rozeti (badge) ekleyin.
- Raporda bir pipeline çalışmasının ekran görüntüsünü ve Summary sayfasındaki `@hata` özetini gösterin.

### 5.8 Rapor

- [RAPOR_SABLONU.md](RAPOR_SABLONU.md) dosyasını **`docs/RAPOR.md`** adıyla kopyalayıp doldurun.
  Dosya adı ve konumu önemli, raporunuzu bu konumda arayacağım.
- Rapor Türkçe ve tablolarla yapılandırılmış olmalı. Ekran görüntülerini `docs/img/` altına koyup
  rapordan bağlantı verin.
- **Bölüm 9 (Bulunan Hatalar)** raporun en önemli kısmı. Her hata için şu bilgileri bekliyorum:

  | Alan | Ne yazılmalı? |
  |------|---------------|
  | Hata No | H-01, H-02 ... |
  | Açıklama | Hatanın tek cümlelik tanımı |
  | İhlal edilen gereksinim | GEREKSINIMLER.md'deki kural kodu (`R-x.y`, `G-x` veya bölüm numarası) |
  | Yeniden üretme adımları | Hatayı tekrar oluşturmak için gereken başlangıç durumu ve işlem sırası |
  | Beklenen / Gerçekleşen | Gereksinime göre ne olmalıydı / uygulama ne yaptı |
  | Yakalayan test | `SinifAdi.testMetoduAdi` veya feature dosyası + senaryo adı |
  | Hangi teknikle bulundu | EP, BVA, karar tablosu, durum geçişi, beyaz kutu, BDD, API, performans, statik analiz |
  | Etki seviyesi | Aşağıdaki seviyelerden biri |

  **Etki seviyeleri:**
  - **Kritik:** para kaybı veya yanlış bakiye
  - **Yüksek:** iş kuralının atlanması (yasaklı işleme izin verilmesi)
  - **Orta:** sınır değerde yanlış davranış veya yanlış hata kodu
  - **Düşük:** kayıt, log veya tutarlılık eksikleri
- **Bölüm 10:** Hangi teknik hangi tür hatayı buldu? Hangi hatayı yalnızca belirli bir teknikle
  bulabildiniz? Projeye yeniden başlasanız neyi farklı yapardınız?

---

## 6. Önerilen Klasör Yapısı (Teslimde)

```
<repo>/
├── docs/
│   ├── RAPOR.md                         ← rapor (zorunlu ad)
│   ├── kapsama/jacoco.csv               ← JaCoCo çıktısı
│   ├── postman/bankaccount.postman_collection.json
│   └── img/                             ← ekran görüntüleri
├── performans/
│   ├── bankaccount-yuk-testi.jmx
│   └── sonuclar/
└── src/test/
    ├── java/tr/edu/sakarya/bil002/
    │   ├── blackbox/   whitebox/   integration/   bdd/
    └── resources/features/              ← en az 5 .feature
```

---

## 7. Teslim Öncesi Kontrol Listesi

- [ ] Repo **public**, **Issues** açık
- [ ] Repo URL'si bana gönderildi
- [ ] `docs/RAPOR.md` mevcut ve tüm bölümler dolu
- [ ] Kara kutu: ≥ 20 senaryo, EP + BVA + karar tablosu + durum geçişi
- [ ] Beyaz kutu: `docs/kapsama/jacoco.csv` commit edildi, `service` paketinde dal kapsamı ≥ %80
- [ ] TDD: red / green / refactor commit'leri görünüyor
- [ ] BDD: örnek hariç ≥ 5 `.feature` dosyası, adımları tanımlı
- [ ] Entegrasyon testleri + `docs/postman/*.postman_collection.json`
- [ ] `performans/*.jmx` + sonuçlar + raporda yorum
- [ ] Hatayı yakalayan tüm testler `@Tag("hata")` / `@hata` ile etiketli
- [ ] `mvn checkstyle:check` ve `mvn test -DexcludedGroups=hata` yerelde başarılı
- [ ] Son commit'te GitHub Actions **yeşil**
- [ ] `src/main/` altında yalnızca TDD özelliği için ekleme yapıldı, kasıtlı hatalar düzeltilmedi
- [ ] Son commit **20 Aralık 2026 Pazar** veya öncesinde

---

## 8. Sık Sorulan Sorular

**Testim kırmızı. Hatayı ben mi yaptım, uygulama mı?**
Testin dayandığı kuralı GEREKSINIMLER.md'de bulun. Kural açıksa ve uygulama ona uymuyorsa bulduğunuz
şey bir uygulama hatasıdır: `@Tag("hata")` ekleyip raporlayın. Kuralı bulamıyorsanız testiniz
muhtemelen belgede olmayan bir varsayıma dayanıyordur.

**Kaç tane kasıtlı hata var?**
Bunu açıklamayacağım. Hata listesini (`docs/HATA_LISTESI.md`) değerlendirme sonrasında paylaşacağım.

**Testler birbirini etkiliyor (bakiye/limit beklenmedik).**
Tüm testler aynı bellek içi veritabanını paylaşır. Her testte **yeni bir hesap** oluşturun ve
yalnızca o hesap üzerinden doğrulama yapın. Sabit bir hesap numarasına güvenmeyin.

**Zamana bağlı kuralları nasıl test ederim?**
Testin içinde gerçekten bir gün bekleyemezsiniz. Testin başlangıç durumunu (verileri) veya zamanı
nasıl kontrol edebileceğinizi düşünün; bu, beyaz kutu bakış açısı gerektirir.

**JaCoCo raporu oluşmuyor.**
Proje yolunuzda Türkçe karakter veya boşluk olabilir (bkz. Bölüm 2). Ayrıca `mvn test` en az bir
kez başarıyla tamamlanmış olmalıdır.

**Pipeline yerelde geçiyor, GitHub'da kırmızı.**
Actions sekmesinde kırmızı adıma tıklayıp loga bakın. En sık nedenler: Checkstyle ihlali, etiketsiz
kırmızı test, `@Tag` import'unun unutulması.

**Değerlendirmeyi nasıl yapacaksınız?**
Son teslim tarihinden sonra reponuzu yukarıdaki başlıklara göre inceleyeceğim. Geri bildirimimi
reponuza bir **Issue** olarak göndereceğim; Issues kapalıysa geri bildirimi alamazsınız.
