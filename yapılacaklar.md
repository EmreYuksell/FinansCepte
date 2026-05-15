# CepteFinans Desktop - Yapılacaklar Listesi

> Son Güncelleme: 13 Mayıs 2026

---

## 1. Mevcut İşlevsel Sayfaların İyileştirilmesi

### Dashboard
- [ ] Kart değişim yüzdelerini gerçek önceki aya göre dinamik hesapla (şu an sabit string).
- [ ] "Son İşlemler" tablosunda çift tıklama ile işlem düzenleme modalını aç.
- [ ] Dashboard verilerini otomatik yenileme (örn. her 60 saniyede bir).

### İşlemler
- [ ] Tarih aralığı filtresini (`DatePicker` Başlangıç/Bitiş) API çağrısına bağla.
- [ ] "Dışa Aktar" butonunu aktif et: **CSV** veya **Excel (.xlsx)** export.
- [ ] Toplu işlem silme (checkbox ile çoklu seçim).
- [ ] İşlem düzenleme modalı (şu an sadece ekleme var, tablodan edit yok).

### Bütçeler
- [ ] Bütçe düzenleme dialogunu aktif et (şu an sadece ekleme var).
- [ ] Aylık / Yıllık filtre dropdown'ı ekle.
- [ ] Bütçe silme işlemine onay dialogu ekle (`Alert`.

### Bildirimler
- [ ] Bildirim detayını gösteren küçük bir panel/slide-out ekle.
- [ ] Okunmamış bildirim sayısı badge'ini sidebar'da da göster.

---

## 2. Hesaplar & Varlıklar Sayfası
**Durum:** Sadece arayüz (placeholder/demo veri). Backend servisi **yok**.

### Backend: Yeni `accounts-service` Mikroservisi
- [ ] Spring Boot projesi oluştur (`accounts-service`).
- [ ] **Entity: Account**
  - `id`, `userId`, `name`, `type` (VADESIZ, BIrikim, YATIRIM, KREDI_KARTI), `institution`, `balance`, `currency`, `createdAt`, `updatedAt`.
- [ ] **Entity: Asset** (Varlık)
  - `id`, `userId`, `name`, `type` (HISSE, KRIPTO, ALTIN, DOVIZ, DIGER), `currentValue`, `purchaseValue`, `quantity`, `currency`, `createdAt`.
- [ ] **Repository:** JPA Repository (PostgreSQL).
- [ ] **REST API Endpointleri:**
  - `GET /api/accounts` → Tüm hesapları listele
  - `POST /api/accounts` → Yeni hesap ekle
  - `PUT /api/accounts/{id}` → Hesap güncelle
  - `DELETE /api/accounts/{id}` → Hesap sil
  - `GET /api/assets` → Tüm varlıkları listele
  - `POST /api/assets` → Yeni varlık ekle
  - `PUT /api/assets/{id}` → Varlık güncelle
  - `DELETE /api/assets/{id}` → Varlık sil
- [ ] **Flyway Migration:** `V1__create_accounts_and_assets.sql`

### Altyapı
- [ ] `api-gateway` → `application.yml`'e `accounts-service` route ekle (`/api/accounts/**`, `/api/assets/**`).
- [ ] `docker-compose.yml`'e `accounts-service` konteyneri ekle.
- [ ] `notification-service` ile entegrasyon: Hesap bakiyesi düşükse bildirim gönder.

### Desktop: Hesaplar Sayfasını API'ye Bağla
- [ ] Hesap kartlarını API'den çek (`GET /api/accounts`).
- [ ] Hesap ekleme modalı: Banka adı, hesap türü, kurum, bakiye, para birimi.
- [ ] Hesap düzenleme / silme.
- [ ] Varlık ekleme modalı: Hisse, kripto, altın, döviz vb.
- [ ] Hesap hareketleri geçmişini `transaction-service`'den filtreleyerek çek.
- [ ] Toplam varlık hesaplamasını API'den gelen veriyle dinamik yap.
- [ ] Yatırım dağılımı pasta grafiğini `assets` verisinden oluştur.

---

## 3. Tasarruf Hedefleri Sayfası
**Durum:** Sadece arayüz (sabit demo veri). Backend servisi **yok**.

### Backend: Yeni `goals-service` Mikroservisi
- [ ] Spring Boot projesi oluştur (`goals-service`).
- [ ] **Entity: Goal**
  - `id`, `userId`, `name`, `targetAmount`, `currentAmount`, `deadline`, `color`, `category`, `createdAt`.
- [ ] **Repository:** JPA Repository (PostgreSQL).
- [ ] **REST API Endpointleri:**
  - `GET /api/goals` → Hedefleri listele
  - `POST /api/goals` → Yeni hedef oluştur
  - `PUT /api/goals/{id}` → Hedef güncelle
  - `PATCH /api/goals/{id}/deposit` → Hedefe para ekle (birikim yap)
  - `DELETE /api/goals/{id}` → Hedef sil
- [ ] **Flyway Migration:** `V1__create_goals.sql`

### Altyapı
- [ ] `api-gateway` → route ekle (`/api/goals/**`).
- [ ] `docker-compose.yml`'e `goals-service` ekle.
- [ ] `notification-service` ile entegrasyon: Hedef %50 veya %100 tamamlandığında bildirim.

### Desktop: Hedef Sayfasını API'ye Bağla
- [ ] Hedef kartlarını API'den dinamik çek.
- [ ] Hedef ekleme formunu API'ye bağla.
- [ ] Hedefe **para ekle (+)** butonu ile `PATCH /deposit` çağrısı yap.
- [ ] İlerleme yüzdesini `(current / target) * 100` olarak gerçek zamanlı hesapla.
- [ ] Hedef silme ve düzenleme.
- [ ] Özet kartları (Toplam Hedef, Biriken, Kalan) API'den gelen veriye göre güncelle.

---

## 4. Raporlar & Analiz Sayfası
**Durum:** Sadece arayüz (örnek veri). Backend servisi **yok**.

### Backend: Yeni `reports-service` Mikroservisi
- [ ] Spring Boot projesi oluştur (`reports-service`).
- [ ] `transaction-service` ve `budget-service`'den veri çeken **Feign Client** veya `WebClient` entegrasyonu.
- [ ] **REST API Endpointleri:**
  - `GET /api/reports/summary?period=30d|3m|6m|1y|all` → Gelir/gider özetleri
  - `GET /api/reports/trend?period=...` → Aylık trend verisi (Line chart için)
  - `GET /api/reports/category?period=...` → Kategori dağılımı (Pie/Bar chart için)
  - `GET /api/reports/insights` → Finansal öngörü/insight'lar (basit algoritma ile)
  - `GET /api/reports/export/pdf?period=...` → PDF rapor indirme (iText / OpenPDF kütüphanesi)
- [ ] **Rapor Algoritmaları:**
  - Ortalama aylık gelir/gider hesaplama.
  - En yüksek gider ayı tespiti.
  - Tasarruf oranı hesaplama (`(gelir - gider) / gelir * 100`).
  - Kategori başına yüzde dağılımı.

### Altyapı
- [ ] `api-gateway` → route ekle (`/api/reports/**`).
- [ ] `docker-compose.yml`'e `reports-service` ekle.

### Desktop: Rapor Sayfasını API'ye Bağla
- [ ] Tüm grafikleri API'den gelen veriyle doldur.
- [ ] Dönem filtresini (`ComboBox`) API query parametresine bağla.
- [ ] Rapor tipi değiştirince (`ComboBox`) ilgili endpoint'i çağır.
- [ ] "PDF İndir" butonunu `GET /api/reports/export/pdf` endpoint'ine bağla.
- [ ] Finansal öngörü (insight) kutularını API'den gelen metinlerle doldur.
- [ ] Özet istatistik kartlarını API'den gelen veriyle güncelle.

---

## 5. Döviz & Kripto Sayfası
**Durum:** Sadece arayüz (sabit demo veri). Backend servisi **yok**.

### Backend: Yeni `currency-service` Mikroservisi
- [ ] Spring Boot projesi oluştur (`currency-service`).
- [ ] **Dış API Entegrasyonları:**
  - Döviz: **ExchangeRate-API** (ücretsiz katman) veya **TCMB**.
  - Kripto: **CoinGecko API** (ücretsiz).
- [ ] **Entity: CurrencyRate**
  - `id`, `symbol`, `name`, `rate`, `changePercent24h`, `high24h`, `low24h`, `lastUpdated`, `type` (FIAT / CRYPTO).
- [ ] **Entity: PriceAlert**
  - `id`, `userId`, `symbol`, `targetPrice`, `condition` (ABOVE / BELOW), `isActive`, `createdAt`.
- [ ] **REST API Endpointleri:**
  - `GET /api/currency/rates` → Anlık kur listesi
  - `GET /api/currency/rates/{symbol}` → Tekil kur
  - `GET /api/currency/alerts` → Kullanıcının alarmları
  - `POST /api/currency/alerts` → Alarm ekle
  - `DELETE /api/currency/alerts/{id}` → Alarm sil
  - `PUT /api/currency/alerts/{id}/toggle` → Aktif/Pasif yap
- [ ] **Background Job (`@Scheduled` / `@EnableScheduling`):**
  - Her **5 dakikada bir** dış API'den kur verilerini çek ve veritabanını güncelle.
  - Her kontrolde aktif alarmları denetle; koşul sağlanıyorsa `notification-service`'e bildirim gönder.
- [ ] **Flyway Migration:** `V1__create_currency_tables.sql`

### Altyapı
- [ ] `api-gateway` → route ekle (`/api/currency/**`).
- [ ] `docker-compose.yml`'e `currency-service` ekle.
- [ ] `notification-service` ile entegrasyon: Fiyat alarmı tetiklendiğinde push bildirim.

### Desktop: Döviz Sayfasını API'ye Bağla
- [ ] Kur kartlarını `GET /api/currency/rates`'den canlı veriyle güncelle.
- [ ] Piyasa özeti tablosunu dinamik yap (sembol, fiyat, düşük, yüksek, değişim, trend).
- [ ] Renk kodlamasını değişim yüzdesine göre dinamik yap (yeşil/kırmızı).
- [ ] Fiyat alarmı ekleme modalı: Sembol seç, hedef fiyat, üstü/altı koşulu.
- [ ] Alarm listesini `GET /api/currency/alerts`'ten çek.
- [ ] Alarm silme ve aktif/pasif yapma.

---

## 6. Ayarlar Sayfası
**Durum:** Sadece arayüz (toggle'lar görsel, hiçbir ayarı kaydetmiyor). Backend servisi **yok**.

### Backend: Yeni `settings-service` Mikroservisi
- [ ] Spring Boot projesi oluştur (`settings-service`).
- [ ] **Entity: UserSettings**
  - `userId`, `darkMode`, `notificationsEnabled`, `emailSummary`, `twoFactorEnabled`, `currencyAlertsEnabled`, `weeklyReportEnabled`, `language`, `currency`.
- [ ] **Entity: UserProfile**
  - `userId`, `fullName`, `email`, `phone`, `avatarUrl`.
- [ ] **REST API Endpointleri:**
  - `GET /api/settings` → Kullanıcı ayarlarını getir
  - `PUT /api/settings` → Ayarları güncelle
  - `GET /api/settings/profile` → Profil bilgileri
  - `PUT /api/settings/profile` → Profili güncelle
  - `POST /api/settings/change-password` → Şifre değiştir
- [ ] **Şifre Değiştirme:** Mevcut şifreyi `service-user`'den doğrulat, yeni şifreyi hash'le kaydet.
- [ ] **Flyway Migration:** `V1__create_settings_and_profile.sql`

### Altyapı
- [ ] `api-gateway` → route ekle (`/api/settings/**`).
- [ ] `docker-compose.yml`'e `settings-service` ekle.

### Desktop: Ayarlar Sayfasını API'ye Bağla
- [ ] Toggle anahtarlarını (`darkMode`, `notifications`, `emailSummary`, `twoFactor`, `currencyAlerts`, `weeklyReport`) API'ye bağla.
  - Toggle değiştiğinde anında `PUT /api/settings` çağrısı yap.
- [ ] Profil bilgilerini (`fullName`, `email`, `phone`) `GET /api/settings/profile`'den çek ve göster.
- [ ] Profil kaydet butonunu `PUT /api/settings/profile`'e bağla.
- [ ] Şifre değiştir formunu `POST /api/settings/change-password`'e bağla.
  - Eski şifre, yeni şifre, tekrar alanları doğrulama.
- [ ] Veri dışa aktar: `GET /api/settings/export` endpoint'ine bağla (JSON formatında).
- [ ] Veri içe aktar: Dosya seç ve `POST /api/settings/import` ile gönder.
- [ ] Tüm verileri sil: Onay dialogu sonrası `DELETE /api/settings/data` çağrısı.

---

## 7. Genel Altyapı & Entegrasyon

### Docker Compose
- [ ] `docker-compose.yml`'e eklenen 5 yeni servisi (`accounts`, `goals`, `reports`, `currency`, `settings`) ekle.
- [ ] Her yeni servis için PostgreSQL veritabanı konteyneri veya mevcut DB'ye yeni schema ekle.
- [ ] Servisler arası network yapılandırmasını kontrol et.

### API Gateway
- [ ] `api-gateway`'e tüm yeni servis route'larını ekle.
- [ ] CORS ayarlarını yeni endpoint'ler için kontrol et.
- [ ] Load balancer yapılandırması (gerekirse).

### Notification Service Entegrasyonu
- [ ] `notification-service`'i yeni servislerle entegre et:
  - Hesap bakiyesi düşükse bildirim.
  - Bütçe aşıldığında bildirim (zaten var, kontrol et).
  - Tasarruf hedefi %50 veya %100 tamamlandığında bildirim.
  - Döviz/kripto fiyat alarmı tetiklendiğinde bildirim.
  - Haftalık rapor hazır olduğunda e-posta bildirimi.

### Test & Kalite
- [ ] Her yeni backend servisi için **unit testler** (JUnit 5, Mockito).
- [ ] Her yeni backend servisi için **integration testler** (Spring Boot Test, Testcontainers).
- [ ] Desktop uygulaması için temel **smoke test** (kritik akışlar).
- [ ] `k6` load test dosyalarını yeni endpoint'ler için genişlet.
- [ ] Tüm sayfaları farklı ekran çözünürlüklerinde ve ölçeklendirmede test et.

---

## Öncelik Sırası (Öneri)

### Faz 1 - Hızlı Kazanımlar
1. Dashboard iyileştirmeleri (kart yüzdeleri, otomatik yenileme).
2. İşlemler sayfası tarih filtresi ve export.
3. Bütçe düzenleme.

### Faz 2 - Hesaplar & Hedefler (En Çok Kullanılan)
4. `accounts-service` backend.
5. Hesaplar & Varlıklar sayfasını API'ye bağla.
6. `goals-service` backend.
7. Tasarruf Hedefleri sayfasını API'ye bağla.

### Faz 3 - Raporlama
8. `reports-service` backend.
9. Raporlar sayfasını API'ye bağla + PDF export.

### Faz 4 - Döviz & Ayarlar
10. `currency-service` backend.
11. Döviz sayfasını API'ye bağla.
12. `settings-service` backend.
13. Ayarlar sayfasını API'ye bağla.

### Faz 5 - Test & Polish
14. Tüm testleri yaz.
15. Performans optimizasyonu.
16. Dokümantasyon güncelleme.

---

## Notlar

- **Teknoloji Stack:**
  - Backend: Java 17, Spring Boot 3.x, PostgreSQL, JPA/Hibernate, Flyway.
  - Desktop: JavaFX 21, Jackson.
  - Gateway: Spring Cloud Gateway.
  - Test: JUnit 5, Mockito, Testcontainers, k6.

- **Dosya Yapısı:**
  - Her yeni servis: `backend/<service-name>/`
  - Desktop: `desktop-app/src/main/java/com/finanscepte/desktop/`
