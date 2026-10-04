# BankAccount API — Sistem Gereksinimleri

> Bu belge sistemin **nasıl çalışması gerektiğini** tanımlar. Test vakalarınızı yazarken
> referans alacağınız **tek kaynak** budur. Uygulama bu belgeden farklı davranıyorsa,
> bu bir **hata**dır ve raporlanmalıdır.

---

## 1. Genel Kurallar

| Kod | Kural |
|-----|-------|
| G-1 | Para birimi Türk Lirası'dır (TL). En küçük birim 1 kuruştur (0,01 TL). |
| G-2 | Tüm tutarlar (`amount`, `initialBalance`) ondalıklı sayıdır. Ondalık karşılaştırmalarda testlerde 0,001 tolerans kullanılabilir. |
| G-3 | Tüm API yanıtları JSON formatındadır. |
| G-4 | Bir işlem reddedildiğinde hesabın bakiyesi ve durumu **hiç değişmemelidir**. |
| G-5 | İşlem birden fazla hesabı etkiliyorsa (transfer) ya tamamı gerçekleşir ya hiçbiri gerçekleşmez (atomiklik). |
| G-6 | "Gün", sunucunun yerel saatine göre 00:00:00 – 23:59:59 aralığıdır. |

---

## 2. Veri Modeli

### 2.1 Hesap (Account)

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | Long | Veritabanı kimliği (otomatik) |
| `accountNumber` | String | **10 haneli**, yalnızca rakamlardan oluşan, ilk hanesi 0 olmayan, **sistem genelinde benzersiz** hesap numarası. Sistem tarafından otomatik üretilir. |
| `ownerName` | String | Hesap sahibinin adı |
| `balance` | Double | Güncel bakiye. Varsayılan: `0.0` |
| `status` | Enum | `ACTIVE`, `FROZEN`, `CLOSED`. Varsayılan: `ACTIVE` |
| `accountType` | String | `STANDARD` veya `PREMIUM` |
| `createdAt` | DateTime | Oluşturulma zamanı |
| `updatedAt` | DateTime | Son güncellenme zamanı |

### 2.2 İşlem (Transaction)

| Alan | Tip | Açıklama |
|------|-----|----------|
| `id` | Long | Veritabanı kimliği (otomatik) |
| `accountNumber` | String | İşlemin ait olduğu hesap |
| `type` | Enum | `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `INTEREST` |
| `amount` | Double | İşlem tutarı (istenen tutar) |
| `balanceAfter` | Double | İşlemden sonraki bakiye (başarısız işlemde değişmemiş bakiye) |
| `description` | String | Açıklama |
| `status` | Enum | `SUCCESS` veya `FAILED` |
| `timestamp` | DateTime | İşlem zamanı |

---

## 3. Hesap Türleri ve Limitler

| Kural | STANDARD | PREMIUM |
|-------|---------:|--------:|
| Tek seferlik **maksimum para yatırma** | 10.000 TL | 50.000 TL |
| **Günlük para çekme** limiti | 5.000 TL | 20.000 TL |
| **Yıllık faiz oranı** | %3 | %5 |

**Limitler kapsayıcıdır (dahil):**

- STANDARD hesaba tek seferde **tam 10.000,00 TL** yatırılabilir; 10.000,01 TL reddedilir.
- PREMIUM hesaba tek seferde **tam 50.000,00 TL** yatırılabilir; 50.000,01 TL reddedilir.
- Bir STANDARD hesaptan bir gün içinde **toplam tam 5.000,00 TL** çekilebilir; toplamı 5.000 TL'yi aşan çekim reddedilir.
- Bir PREMIUM hesaptan bir gün içinde **toplam tam 20.000,00 TL** çekilebilir; toplamı 20.000 TL'yi aşan çekim reddedilir.

Günlük çekim limiti hesaplanırken **yalnızca o gün (bkz. G-6) yapılmış başarılı (`SUCCESS`)
`WITHDRAWAL` işlemleri** toplanır. Önceki günlerin çekimleri, transferler ve başarısız denemeler
limite dahil **edilmez**. Her gün limit sıfırlanır.

```
Günlük çekim kontrolü:  (bugün yapılan başarılı çekimlerin toplamı + yeni çekim tutarı) <= günlük limit
```

---

## 4. Hesap Durumları ve Geçişler

### 4.1 Durum Geçiş Diyagramı

```
                    createAccount()
                          |
                          v
                   +-------------+
          +------->|   ACTIVE    |--------------------+
          |        +-------------+                    |
          |              |                            |
   unfreezeAccount()     | freezeAccount()            | closeAccount()
          |              v                            | [bakiye == 0]
          |        +-------------+                    |
          +--------|   FROZEN    |                    |
                   +-------------+                    |
                         |                            |
                         | closeAccount()             |
                         | [bakiye == 0]              |
                         v                            v
                   +---------------------------------------+
                   |               CLOSED                  |
                   |   (son durum — hiçbir yere geçemez)   |
                   +---------------------------------------+
```

### 4.2 Durum Geçiş Tablosu

| Mevcut Durum | Olay | Koşul | Yeni Durum | Sonuç |
|--------------|------|-------|------------|-------|
| ACTIVE | freezeAccount | — | FROZEN | Başarılı |
| ACTIVE | unfreezeAccount | — | ACTIVE | **Reddedilir** |
| ACTIVE | closeAccount | bakiye = 0 | CLOSED | Başarılı |
| ACTIVE | closeAccount | bakiye ≠ 0 | ACTIVE | **Reddedilir** |
| FROZEN | freezeAccount | — | FROZEN | **Reddedilir** (zaten dondurulmuş) |
| FROZEN | unfreezeAccount | — | ACTIVE | Başarılı |
| FROZEN | closeAccount | bakiye = 0 | CLOSED | Başarılı |
| FROZEN | closeAccount | bakiye ≠ 0 | FROZEN | **Reddedilir** |
| CLOSED | freezeAccount | — | CLOSED | **Reddedilir** |
| CLOSED | unfreezeAccount | — | CLOSED | **Reddedilir** (kapalı hesap tekrar açılamaz) |
| CLOSED | closeAccount | — | CLOSED | **Reddedilir** (zaten kapalı) |

### 4.3 Duruma Göre İzin Verilen İşlemler

| İşlem | ACTIVE | FROZEN | CLOSED |
|-------|:------:|:------:|:------:|
| Hesap sorgulama / işlem geçmişi | ✔ | ✔ | ✔ |
| Para yatırma (deposit) | ✔ | ✘ | ✘ |
| Para çekme (withdraw) | ✔ | ✘ | ✘ |
| Transfer gönderme (kaynak hesap) | ✔ | ✘ | ✘ |
| Transfer alma (hedef hesap) | ✔ | ✘ | ✘ |
| Faiz uygulama | ✔ | ✘ | ✘ |

FROZEN hesapta bakiye **korunur**; dondurma ve çözme işlemleri bakiyeyi değiştirmez.

---

## 5. Fonksiyonel Gereksinimler

Aşağıda "reddedilir" ifadesi, işlemin gerçekleştirilmediği, bakiyenin/durumun değişmediği ve
Bölüm 7'deki uygun hatanın döndüğü anlamına gelir.

### 5.1 Hesap Açma — `createAccount(ownerName, accountType, initialBalance)`

| Kod | Kural |
|-----|-------|
| R-1.1 | `ownerName` `null`, boş (`""`) veya yalnızca boşluklardan oluşuyorsa reddedilir. |
| R-1.2 | `accountType` yalnızca `"STANDARD"` veya `"PREMIUM"` olabilir (büyük harf, birebir eşleşme). Diğer tüm değerler ve `null` reddedilir. |
| R-1.3 | `initialBalance` `null` ise bakiye `0.0` kabul edilir. |
| R-1.4 | `initialBalance` **negatif** ise reddedilir. `0` ve pozitif değerler kabul edilir. |
| R-1.5 | Sistem benzersiz, 10 haneli bir `accountNumber` üretir. Hiçbir koşulda (eşzamanlı istekler dahil) iki hesap aynı numarayı almamalıdır. |
| R-1.6 | Hesap `ACTIVE` durumda oluşturulur. |
| R-1.7 | Hesap açılışı için işlem (Transaction) kaydı oluşturulmaz. |

**Çıktı:** Oluşturulan hesap.

### 5.2 Para Yatırma — `deposit(accountNumber, amount)`

| Kod | Kural |
|-----|-------|
| R-2.1 | Hesap mevcut değilse "hesap bulunamadı" hatası döner. |
| R-2.2 | `amount` **kesinlikle sıfırdan büyük** olmalıdır. `amount <= 0` (0 dahil) reddedilir. |
| R-2.3 | Hesap `ACTIVE` olmalıdır. `FROZEN` veya `CLOSED` hesaba yatırma reddedilir. |
| R-2.4 | `amount`, hesap türünün tek seferlik maksimum yatırma limitini (Bölüm 3) aşamaz. |
| R-2.5 | Başarılı işlemde: `yeni bakiye = eski bakiye + amount`. |
| R-2.6 | Başarılı işlemde `DEPOSIT` tipinde, `SUCCESS` durumunda işlem kaydı oluşturulur. |
| R-2.7 | Reddedilen denemede `DEPOSIT` tipinde, `FAILED` durumunda işlem kaydı oluşturulur (bkz. Bölüm 6). |

**Çıktı:** Güncellenmiş hesap.

### 5.3 Para Çekme — `withdraw(accountNumber, amount)`

| Kod | Kural |
|-----|-------|
| R-3.1 | Hesap mevcut değilse "hesap bulunamadı" hatası döner. |
| R-3.2 | `amount` kesinlikle sıfırdan büyük olmalıdır. `amount <= 0` reddedilir. |
| R-3.3 | Hesap `ACTIVE` olmalıdır. `FROZEN` veya `CLOSED` hesaptan çekim reddedilir. |
| R-3.4 | `amount <= bakiye` olmalıdır. **Bakiyenin tamamı çekilebilir** (çekim sonrası bakiye 0 olabilir). Bakiyeden fazla çekim reddedilir. |
| R-3.5 | Günlük çekim limiti (Bölüm 3) aşılamaz. |
| R-3.6 | Başarılı işlemde: `yeni bakiye = eski bakiye − amount`. |
| R-3.7 | Başarılı işlemde `WITHDRAWAL` tipinde, `SUCCESS` durumunda işlem kaydı oluşturulur. |
| R-3.8 | Reddedilen denemede `WITHDRAWAL` tipinde, `FAILED` durumunda işlem kaydı oluşturulur (bkz. Bölüm 6). |

**Çıktı:** Güncellenmiş hesap.

### 5.4 Transfer — `transfer(fromAccount, toAccount, amount)`

| Kod | Kural |
|-----|-------|
| R-4.1 | `amount` kesinlikle sıfırdan büyük olmalıdır. |
| R-4.2 | Her iki hesap da mevcut olmalıdır; biri bile yoksa "hesap bulunamadı" hatası döner. |
| R-4.3 | **Her iki hesap da** `ACTIVE` olmalıdır. Kaynak veya hedef hesap `FROZEN`/`CLOSED` ise transfer reddedilir. |
| R-4.4 | Bir hesaptan **aynı hesaba** transfer reddedilir (`fromAccount == toAccount`). |
| R-4.5 | Kaynak hesabın bakiyesi yeterli olmalıdır (`amount <= kaynak bakiye`). Bakiyenin tamamı transfer edilebilir. |
| R-4.6 | Başarılı işlemde kaynak bakiyeden `amount` düşülür, hedef bakiyeye `amount` eklenir. |
| R-4.7 | Başarılı işlemde **her iki hesap için de** `TRANSFER` tipinde `SUCCESS` işlem kaydı oluşturulur. |
| R-4.8 | Transfer, günlük para çekme limitine dahil edilmez; transfer için ayrı bir limit yoktur. |

**Çıktı:** Başarı mesajı.

### 5.5 Hesap Dondurma — `freezeAccount(accountNumber)`

| Kod | Kural |
|-----|-------|
| R-5.1 | Hesap `ACTIVE` olmalıdır. Zaten `FROZEN` veya `CLOSED` olan hesap için reddedilir. |
| R-5.2 | Durum `FROZEN` yapılır. Bakiye değişmez. |

### 5.6 Hesap Dondurmayı Kaldırma — `unfreezeAccount(accountNumber)`

| Kod | Kural |
|-----|-------|
| R-6.1 | Hesap `FROZEN` olmalıdır. `ACTIVE` veya `CLOSED` hesap için reddedilir. |
| R-6.2 | Durum `ACTIVE` yapılır. Bakiye değişmez. |

### 5.7 Hesap Kapatma — `closeAccount(accountNumber)`

| Kod | Kural |
|-----|-------|
| R-7.1 | Hesap `ACTIVE` veya `FROZEN` olmalıdır. Zaten `CLOSED` ise reddedilir. |
| R-7.2 | Bakiye **tam olarak 0** olmalıdır. Bakiyesi olan hesap kapatılamaz (müşterinin parası kaybolmamalıdır). |
| R-7.3 | Durum `CLOSED` yapılır. |
| R-7.4 | `CLOSED` son durumdur; hesap hiçbir yolla tekrar açılamaz. |

### 5.8 Faiz Hesaplama — `calculateInterest(accountNumber)`

| Kod | Kural |
|-----|-------|
| R-8.1 | Hesap `ACTIVE` olmalıdır. `FROZEN` ve `CLOSED` hesaplara faiz uygulanmaz (reddedilir). |
| R-8.2 | Yıllık faiz oranı: STANDARD **%3**, PREMIUM **%5**. |
| R-8.3 | Aylık faiz aşağıdaki formülle hesaplanır ve **kuruşa yuvarlanır** (2 ondalık, yarım yukarı). |
| R-8.4 | Faiz bakiyeye eklenir. |
| R-8.5 | `INTEREST` tipinde, `SUCCESS` durumunda işlem kaydı oluşturulur (`amount` = faiz tutarı). |

**Formül:**

```
aylık_faiz = (bakiye × yıllık_oran) / 12

yıllık_oran:  STANDARD = 0.03   PREMIUM = 0.05
yeni_bakiye = bakiye + aylık_faiz
```

**Örnekler:**

| Hesap Türü | Bakiye | Hesap | Aylık Faiz | Yeni Bakiye |
|------------|-------:|-------|-----------:|------------:|
| STANDARD | 1.000,00 | 1000 × 0,03 / 12 | 2,50 | 1.002,50 |
| PREMIUM | 1.000,00 | 1000 × 0,05 / 12 | 4,17 | 1.004,17 |
| STANDARD | 12.000,00 | 12000 × 0,03 / 12 | 30,00 | 12.030,00 |
| PREMIUM | 2.500,50 | 2500,50 × 0,05 / 12 | 10,42 | 2.510,92 |
| STANDARD | 0,00 | 0 × 0,03 / 12 | 0,00 | 0,00 |

### 5.9 İşlem Geçmişi — `getTransactionHistory(accountNumber)`

| Kod | Kural |
|-----|-------|
| R-9.1 | Hesap mevcut değilse "hesap bulunamadı" hatası döner. |
| R-9.2 | Hesaba ait **tüm** işlemler (`SUCCESS` ve `FAILED`) döndürülür. |
| R-9.3 | Sıralama: tarihe göre **azalan** (en yeni işlem ilk sırada). |
| R-9.4 | Hiç işlem yoksa boş liste döner. Hesap durumu (ACTIVE/FROZEN/CLOSED) sorgulamayı engellemez. |

### 5.10 Hesap Sorgulama — `GET /api/accounts/{accountNumber}`

| Kod | Kural |
|-----|-------|
| R-10.1 | Hesap mevcutsa hesap bilgisi `200 OK` ile döner. |
| R-10.2 | Hesap mevcut değilse **`404 Not Found`** döner. |

---

## 6. İşlem (Transaction) Kaydı Ne Zaman Oluşturulur?

| Durum | Kayıt Oluşur mu? | Tip | Status | `balanceAfter` |
|-------|:----------------:|-----|--------|----------------|
| Hesap açma | Hayır | — | — | — |
| Başarılı para yatırma | Evet | DEPOSIT | SUCCESS | Yeni bakiye |
| Başarılı para çekme | Evet | WITHDRAWAL | SUCCESS | Yeni bakiye |
| Başarılı transfer | Evet (**2 kayıt**: kaynak ve hedef) | TRANSFER | SUCCESS | İlgili hesabın yeni bakiyesi |
| Faiz uygulama | Evet | INTEREST | SUCCESS | Yeni bakiye |
| Reddedilen para yatırma denemesi¹ | **Evet** | DEPOSIT | **FAILED** | Değişmemiş bakiye |
| Reddedilen para çekme denemesi¹ | **Evet** | WITHDRAWAL | **FAILED** | Değişmemiş bakiye |
| Mevcut olmayan hesaba yapılan istek | Hayır | — | — | — |
| Dondurma / çözme / kapatma | Hayır | — | — | — |

¹ Var olan bir hesap üzerinde, sayısal bir `amount` ile yapılan ve bir iş kuralı nedeniyle
(geçersiz tutar, hesap durumu, yetersiz bakiye, limit aşımı) reddedilen denemeler.
`FAILED` kayıtlar bakiyeyi değiştirmez ve günlük limit hesabına dahil edilmez.

---

## 7. Hata Durumlarında Beklenen Davranış

Hata durumunda API aşağıdaki formatta JSON döndürür:

```json
{
  "timestamp": "2026-10-04T10:15:30",
  "status": 409,
  "error": "Conflict",
  "message": "Hesap aktif değil: 1234567890 (FROZEN)",
  "path": "/api/accounts/1234567890/deposit"
}
```

| Hata Durumu | HTTP Kodu |
|-------------|-----------|
| Geçersiz girdi (boş ad, geçersiz hesap türü, negatif başlangıç bakiyesi, `amount <= 0`, aynı hesaba transfer, eksik alan, bozuk JSON) | `400 Bad Request` |
| Hesap bulunamadı | `404 Not Found` |
| Hesap durumu işleme uygun değil (FROZEN/CLOSED hesapta işlem, geçersiz durum geçişi, bakiyesi olan hesabı kapatma) | `409 Conflict` |
| Yetersiz bakiye, yatırma limiti veya günlük çekim limiti aşımı | `422 Unprocessable Entity` |
| Beklenmeyen sunucu hatası | `500 Internal Server Error` |

Kurallar:

- Hata durumunda bakiye ve hesap durumu değişmez (G-4).
- Servis katmanında her hata bir istisna (exception) olarak fırlatılır; API katmanı bunu yukarıdaki HTTP koduna çevirir.
