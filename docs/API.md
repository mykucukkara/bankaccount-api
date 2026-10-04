# BankAccount API — Endpoint Referansı

- **Temel URL:** `http://localhost:8080`
- **İçerik tipi:** Tüm istek ve yanıtlar `application/json`
- **Hata formatı ve HTTP kodları:** bkz. [GEREKSINIMLER.md — Bölüm 7](GEREKSINIMLER.md#7-hata-durumlarında-beklenen-davranış)

## Özet

| Metot | Yol | Açıklama | Başarılı Yanıt |
|-------|-----|----------|----------------|
| POST | `/api/accounts` | Yeni hesap aç | `201 Created` |
| GET | `/api/accounts` | Tüm hesapları listele | `200 OK` |
| GET | `/api/accounts/{accountNumber}` | Hesap bilgisi | `200 OK` |
| POST | `/api/accounts/{accountNumber}/deposit` | Para yatır | `200 OK` |
| POST | `/api/accounts/{accountNumber}/withdraw` | Para çek | `200 OK` |
| POST | `/api/accounts/{accountNumber}/freeze` | Hesabı dondur | `200 OK` |
| POST | `/api/accounts/{accountNumber}/unfreeze` | Dondurmayı kaldır | `200 OK` |
| POST | `/api/accounts/{accountNumber}/close` | Hesabı kapat | `200 OK` |
| POST | `/api/accounts/{accountNumber}/interest` | Aylık faiz uygula | `200 OK` |
| GET | `/api/accounts/{accountNumber}/transactions` | İşlem geçmişi | `200 OK` |
| POST | `/api/transactions/transfer` | Hesaplar arası transfer | `200 OK` |
| GET | `/api/transactions/{accountNumber}` | İşlem geçmişi (alternatif yol) | `200 OK` |

---

## Hesaplar

### POST `/api/accounts` — Hesap Aç

**İstek:**

```json
{
  "ownerName": "Ayşe Yılmaz",
  "accountType": "STANDARD",
  "initialBalance": 1000.0
}
```

| Alan | Zorunlu | Açıklama |
|------|:-------:|----------|
| `ownerName` | Evet | Boş olamaz |
| `accountType` | Evet | `STANDARD` veya `PREMIUM` |
| `initialBalance` | Hayır | Gönderilmezse `0.0`; negatif olamaz |

**Yanıt — `201 Created`:**

```json
{
  "id": 1,
  "accountNumber": "4821093375",
  "ownerName": "Ayşe Yılmaz",
  "balance": 1000.0,
  "status": "ACTIVE",
  "accountType": "STANDARD",
  "createdAt": "2026-10-04T10:15:30.123",
  "updatedAt": "2026-10-04T10:15:30.123"
}
```

**Hatalar:** `400` (geçersiz ad / tür / negatif bakiye)

### GET `/api/accounts` — Tüm Hesaplar

**Yanıt — `200 OK`:** Hesap nesnelerinden oluşan dizi.

### GET `/api/accounts/{accountNumber}` — Hesap Bilgisi

**Yanıt — `200 OK`:** Hesap nesnesi.
**Hatalar:** `404` (hesap bulunamadı)

### POST `/api/accounts/{accountNumber}/deposit` — Para Yatır

**İstek:**

```json
{ "amount": 500.0 }
```

**Yanıt — `200 OK`:** Güncellenmiş hesap nesnesi.
**Hatalar:** `400` (eksik/geçersiz tutar), `404`, `409` (hesap ACTIVE değil), `422` (yatırma limiti)

### POST `/api/accounts/{accountNumber}/withdraw` — Para Çek

**İstek:**

```json
{ "amount": 200.0 }
```

**Yanıt — `200 OK`:** Güncellenmiş hesap nesnesi.
**Hatalar:** `400`, `404`, `409` (hesap ACTIVE değil), `422` (yetersiz bakiye / günlük limit)

### POST `/api/accounts/{accountNumber}/freeze` — Dondur

İstek gövdesi yok. **Yanıt — `200 OK`:** Güncellenmiş hesap.
**Hatalar:** `404`, `409` (hesap ACTIVE değil)

### POST `/api/accounts/{accountNumber}/unfreeze` — Dondurmayı Kaldır

İstek gövdesi yok. **Yanıt — `200 OK`:** Güncellenmiş hesap.
**Hatalar:** `404`, `409` (hesap FROZEN değil)

### POST `/api/accounts/{accountNumber}/close` — Kapat

İstek gövdesi yok. **Yanıt — `200 OK`:** Güncellenmiş hesap (`status: CLOSED`).
**Hatalar:** `404`, `409` (zaten kapalı veya bakiye sıfır değil)

### POST `/api/accounts/{accountNumber}/interest` — Faiz Uygula

İstek gövdesi yok. **Yanıt — `200 OK`:** Faiz eklenmiş hesap.
**Hatalar:** `404`, `409` (hesap ACTIVE değil)

### GET `/api/accounts/{accountNumber}/transactions` — İşlem Geçmişi

**Yanıt — `200 OK`:** En yeniden eskiye sıralı işlem dizisi.

```json
[
  {
    "id": 2,
    "accountNumber": "4821093375",
    "type": "WITHDRAWAL",
    "amount": 200.0,
    "balanceAfter": 1300.0,
    "description": "Para çekme",
    "status": "SUCCESS",
    "timestamp": "2026-10-04T10:20:00.456"
  },
  {
    "id": 1,
    "accountNumber": "4821093375",
    "type": "DEPOSIT",
    "amount": 500.0,
    "balanceAfter": 1500.0,
    "description": "Para yatırma",
    "status": "SUCCESS",
    "timestamp": "2026-10-04T10:18:00.789"
  }
]
```

**Hatalar:** `404`

---

## İşlemler

### POST `/api/transactions/transfer` — Transfer

**İstek:**

```json
{
  "fromAccount": "4821093375",
  "toAccount": "7710342291",
  "amount": 250.0
}
```

**Yanıt — `200 OK`:**

```json
{
  "message": "Transfer başarılı",
  "fromAccount": "4821093375",
  "toAccount": "7710342291",
  "amount": 250.0
}
```

**Hatalar:** `400` (geçersiz tutar, aynı hesap, eksik alan), `404`, `409` (hesaplardan biri ACTIVE değil), `422` (yetersiz bakiye)

### GET `/api/transactions/{accountNumber}` — İşlem Geçmişi

`GET /api/accounts/{accountNumber}/transactions` ile aynıdır.

---

## Örnek cURL Komutları

```bash
# Hesap aç
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName":"Ayşe Yılmaz","accountType":"STANDARD","initialBalance":1000}'

# Para yatır
curl -X POST http://localhost:8080/api/accounts/4821093375/deposit \
  -H "Content-Type: application/json" -d '{"amount":500}'

# İşlem geçmişi
curl http://localhost:8080/api/accounts/4821093375/transactions
```
