package com.finanscepte.currency.service;

import com.finanscepte.currency.model.CurrencyRate;
import com.finanscepte.currency.model.PriceAlert;
import com.finanscepte.currency.repository.CurrencyRateRepository;
import com.finanscepte.currency.repository.PriceAlertRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyUpdateService {

    private final CurrencyRateRepository currencyRateRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final CurrencyApiService currencyApiService;
    private final RestTemplate restTemplate;

    private static final String NOTIFICATION_URL = "http://notification-service:8086/api/notifications";

    public CurrencyUpdateService(CurrencyRateRepository currencyRateRepository,
                                 PriceAlertRepository priceAlertRepository,
                                 CurrencyApiService currencyApiService) {
        this.currencyRateRepository = currencyRateRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.currencyApiService = currencyApiService;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(fixedRate = 300000)
    public void updateRates() {
        Map<String, Double> fiatRates = currencyApiService.fetchFiatRates();
        for (Map.Entry<String, Double> entry : fiatRates.entrySet()) {
            upsertRate(entry.getKey(), entry.getKey() + "/TRY", "FIAT", entry.getValue(), 0.0);
        }

        Map<String, CurrencyApiService.CryptoRate> cryptoRates = currencyApiService.fetchCryptoRates();
        for (Map.Entry<String, CurrencyApiService.CryptoRate> entry : cryptoRates.entrySet()) {
            CurrencyApiService.CryptoRate cr = entry.getValue();
            upsertRate(entry.getKey(), entry.getKey() + "/TRY", "CRYPTO", cr.price(), cr.changePercent24h());
        }

        checkPriceAlerts();
    }

    private void upsertRate(String symbol, String name, String type, double rate, double changePercent) {
        CurrencyRate existing = currencyRateRepository.findBySymbol(symbol);
        if (existing == null) {
            existing = CurrencyRate.builder()
                    .symbol(symbol)
                    .name(name)
                    .rate(rate)
                    .changePercent24h(changePercent)
                    .high24h(rate * 1.02)
                    .low24h(rate * 0.98)
                    .lastUpdated(LocalDateTime.now())
                    .type(type)
                    .build();
        } else {
            existing.setRate(rate);
            existing.setChangePercent24h(changePercent);
            existing.setHigh24h(Math.max(existing.getHigh24h(), rate));
            existing.setLow24h(Math.min(existing.getLow24h(), rate));
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
            if (rate == null) continue;

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
