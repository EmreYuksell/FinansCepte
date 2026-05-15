# CepteFinans - Demo Veri Yukleme (Tek Kullanici)
$GW = "http://localhost:8080"

function Post($p, $b) {
    try {
        $r = Invoke-WebRequest -Uri "$GW$p" -Method POST -Body $b -ContentType "application/json" -UseBasicParsing -TimeoutSec 5
        Write-Host "  + $p" -ForegroundColor Green
        return ($r.Content | ConvertFrom-Json).id
    } catch { Write-Host "  X $p - $_" -ForegroundColor Red }
}

Write-Host "=== KULLANICI ===" -ForegroundColor Cyan
$u = Post "/api/users" '{"name":"Emre Yuksel","email":"emreyuksell78@gmail.com","password":"123"}'
if (-not $u) { Write-Host "Kullanici olusturulamadi, cikiliyor." -ForegroundColor Red; exit 1 }

Write-Host "`n=== URUNLER ===" -ForegroundColor Cyan
$p1 = Post "/api/products" '{"name":"Netflix Premium","description":"4K streaming","price":199.99,"category":"eglence"}'
$p2 = Post "/api/products" '{"name":"Spotify","description":"Music","price":59.99,"category":"eglence"}'
$p3 = Post "/api/products" '{"name":"Market","description":"Haftalik market","price":500.00,"category":"gida"}'
$p4 = Post "/api/products" '{"name":"Akaryakit","description":"Benzin","price":800.00,"category":"ulasim"}'
$p5 = Post "/api/products" '{"name":"Fatura","description":"Elektrik/Su/Internet","price":650.00,"category":"fatura"}'
$p6 = Post "/api/products" '{"name":"Spor Salonu","description":"Aylik uyelik","price":350.00,"category":"saglik"}'

Write-Host "`n=== BUTCELER (Mayis 2026) ===" -ForegroundColor Cyan
Post "/api/budgets" "{`"userId`":`"$u`",`"category`":`"gida`",`"limitAmount`":3000,`"month`":5,`"year`":2026}"
Post "/api/budgets" "{`"userId`":`"$u`",`"category`":`"ulasim`",`"limitAmount`":1500,`"month`":5,`"year`":2026}"
Post "/api/budgets" "{`"userId`":`"$u`",`"category`":`"eglence`",`"limitAmount`":800,`"month`":5,`"year`":2026}"
Post "/api/budgets" "{`"userId`":`"$u`",`"category`":`"fatura`",`"limitAmount`":1000,`"month`":5,`"year`":2026}"
Post "/api/budgets" "{`"userId`":`"$u`",`"category`":`"saglik`",`"limitAmount`":500,`"month`":5,`"year`":2026}"

Write-Host "`n=== ISLEMLER ===" -ForegroundColor Cyan
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":25000,`"type`":`"GELIR`",`"description`":`"Maas`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":3000,`"type`":`"GELIR`",`"description`":`"Freelance`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":1500,`"type`":`"GELIR`",`"description`":`"Kira Geliri`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":4500,`"type`":`"GIDER`",`"description`":`"Kira`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":2200,`"type`":`"GIDER`",`"description`":`"Market`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":850,`"type`":`"GIDER`",`"description`":`"Akaryakit`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":650,`"type`":`"GIDER`",`"description`":`"Elektrik`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":350,`"type`":`"GIDER`",`"description`":`"Internet`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":200,`"type`":`"GIDER`",`"description`":`"Netflix`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":60,`"type`":`"GIDER`",`"description`":`"Spotify`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":350,`"type`":`"GIDER`",`"description`":`"Spor Salonu`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":1200,`"type`":`"GIDER`",`"description`":`"Kredi Karti`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":450,`"type`":`"GIDER`",`"description`":`"Disarida Yemek`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":300,`"type`":`"GIDER`",`"description`":`"Su Faturasi`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":800,`"type`":`"GIDER`",`"description`":`"Giyim`"}"
Post "/api/transactions" "{`"userId`":`"$u`",`"amount`":5000,`"type`":`"GIDER`",`"description`":`"Tatil`"}"

Write-Host "`n=== ABONELIKLER ===" -ForegroundColor Cyan
Post "/api/subscriptions" "{`"userId`":`"$u`",`"productId`":`"$p1`",`"startDate`":`"2026-01-01T00:00:00`",`"endDate`":`"2026-12-31T00:00:00`",`"amount`":199.99}"
Post "/api/subscriptions" "{`"userId`":`"$u`",`"productId`":`"$p2`",`"startDate`":`"2026-03-01T00:00:00`",`"endDate`":`"2026-12-31T00:00:00`",`"amount`":59.99}"

Write-Host "`n=== BILDIRIMLER ===" -ForegroundColor Cyan
Post "/api/notifications" "{`"userId`":`"$u`",`"type`":`"WARNING`",`"message`":`"Gida butcenizin %80i harcandi`"}"
Post "/api/notifications" "{`"userId`":`"$u`",`"type`":`"INFO`",`"message`":`"Maas odemesi hesaba yatirildi`"}"
Post "/api/notifications" "{`"userId`":`"$u`",`"type`":`"WARNING`",`"message`":`"Ulasim butcenizi astiniz`"}"
Post "/api/notifications" "{`"userId`":`"$u`",`"type`":`"REMINDER`",`"message`":`"Fatura son odeme: 25 Mayis`"}"

Write-Host "`n=== RAPORLAR ===" -ForegroundColor Cyan
Post "/api/reports" "{`"userId`":`"$u`",`"type`":`"MONTHLY_SUMMARY`",`"startDate`":`"2026-05-01T00:00:00`",`"endDate`":`"2026-05-31T00:00:00`"}"
Post "/api/reports" "{`"userId`":`"$u`",`"type`":`"CATEGORY_BREAKDOWN`",`"startDate`":`"2026-05-01T00:00:00`",`"endDate`":`"2026-05-31T00:00:00`"}"

Write-Host "`n=== TAMAMLANDI ===" -ForegroundColor Cyan
Write-Host "Kullanici: emreyuksell78@gmail.com / 123" -ForegroundColor Yellow
