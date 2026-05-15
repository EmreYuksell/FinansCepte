package com.finanscepte.currency.service;

import com.finanscepte.currency.model.CurrencyRate;
import com.finanscepte.currency.model.PriceAlert;
import com.finanscepte.currency.repository.CurrencyRateRepository;
import com.finanscepte.currency.repository.PriceAlertRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class CurrencyUpdateService {

    private static final String NOTIFICATION_URL = "http://notification-service:8086/api/notifications";

    private final CurrencyRateRepository currencyRateRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final CurrencyApiService currencyApiService;
    private final RestTemplate restTemplate;

    public CurrencyUpdateService(CurrencyRateRepository currencyRateRepository,
                                 PriceAlertRepository priceAlertRepository,
                                 CurrencyApiService currencyApiService,
                                 RestTemplate restTemplate) {
        this.currencyRateRepository = currencyRateRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.currencyApiService = currencyApiService;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initRatesOnStartup() {
        refreshRates();
    }

    @Scheduled(fixedRate = 120_000)
    public void scheduledRefresh() {
        refreshRates();
    }

    public void refreshRates() {
        Map<String, Double> fiatRates = currencyApiService.fetchFiatRates();
        for (Map.Entry<String, Double> entry : fiatRates.entrySet()) {
            double rate = entry.getValue();
            upsertRate(entry.getKey(), entry.getKey() + "/TRY", "FIAT", rate, 0.0, rate, rate);
        }

        Map<String, CurrencyApiService.CryptoRate> cryptoRates = currencyApiService.fetchCryptoRates();
        for (Map.Entry<String, CurrencyApiService.CryptoRate> entry : cryptoRates.entrySet()) {
            CurrencyApiService.CryptoRate cr = entry.getValue();
            double high = cr.high24h() > 0 ? cr.high24h() : cr.price();
            double low = cr.low24h() > 0 ? cr.low24h() : cr.price();
            upsertRate(entry.getKey(), entry.getKey() + "/TRY", "CRYPTO",
                    cr.price(), cr.changePercent24h(), high, low);
        }

        checkPriceAlerts();
    }

    public boolean isDataStale() {
        List<CurrencyRate> rates = currencyRateRepository.findAll();
        if (rates.isEmpty()) {
            return true;
        }
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        Optional<LocalDateTime> latest = rates.stream()
                .map(CurrencyRate::getLastUpdated)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        return latest.map(l -> l.isBefore(threshold)).orElse(true);
    }

    private void upsertRate(String symbol, String name, String type, double rate,
                            double changePercent, double high24h, double low24h) {
        CurrencyRate existing = currencyRateRepository.findBySymbol(symbol);
        if (existing == null) {
            existing = CurrencyRate.builder()
                    .symbol(symbol)
                    .name(name)
                    .rate(rate)
                    .changePercent24h(changePercent)
                    .high24h(high24h)
                    .low24h(low24h)
                    .lastUpdated(LocalDateTime.now())
                    .type(type)
                    .build();
        } else {
            existing.setRate(rate);
            existing.setChangePercent24h(changePercent);
            existing.setHigh24h(high24h);
            existing.setLow24h(low24h);
            existing.setLastUpdated(LocalDateTime.now());
        }
        currencyRateRepository.save(existing);
    }

    private void checkPriceAlerts() {
        List<PriceAlert> activeAlerts = priceAlertRepository.findAll()
                .stream()
                .filter(PriceAlert::isActive)
                .toList();

        for (PriceAlert alert : activeAlerts) {
            CurrencyRate rate = currencyRateRepository.findBySymbol(alert.getSymbol());
            if (rate == null) {
                continue;
            }

            boolean triggered = false;
            if ("ABOVE".equalsIgnoreCase(alert.getCondition()) && rate.getRate() >= alert.getTargetPrice()) {
                triggered = true;
            } else if ("BELOW".equalsIgnoreCase(alert.getCondition()) && rate.getRate() <= alert.getTargetPrice()) {
                triggered = true;
            }

            if (triggered) {
                sendNotification(alert, rate);
                alert.setActive(false);
                priceAlertRepository.save(alert);
            }
        }
    }

    private void sendNotification(PriceAlert alert, CurrencyRate rate) {
        try {
            String msg = String.format("%s fiyatı hedefinize ulaştı: ₺%.2f (koşul: %s %.2f)",
                    alert.getSymbol(), rate.getRate(), alert.getCondition(), alert.getTargetPrice());
            Map<String, String> body = Map.of(
                    "userId", alert.getUserId(),
                    "type", "PRICE_ALERT:" + alert.getSymbol(),
                    "message", msg
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForObject(NOTIFICATION_URL, new HttpEntity<>(body, headers), Void.class);
        } catch (Exception ignored) {
        }
    }
}
