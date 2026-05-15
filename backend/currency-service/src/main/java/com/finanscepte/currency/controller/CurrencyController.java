package com.finanscepte.currency.controller;

import com.finanscepte.currency.model.CurrencyRate;
import com.finanscepte.currency.model.PriceAlert;
import com.finanscepte.currency.repository.CurrencyRateRepository;
import com.finanscepte.currency.repository.PriceAlertRepository;
import com.finanscepte.currency.service.CurrencyUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyRateRepository currencyRateRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final CurrencyUpdateService currencyUpdateService;

    public CurrencyController(CurrencyRateRepository currencyRateRepository,
                              PriceAlertRepository priceAlertRepository,
                              CurrencyUpdateService currencyUpdateService) {
        this.currencyRateRepository = currencyRateRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.currencyUpdateService = currencyUpdateService;
    }

    @GetMapping("/rates")
    public ResponseEntity<List<CurrencyRate>> getRates(
            @RequestParam(defaultValue = "false") boolean refresh) {
        if (refresh || currencyRateRepository.findAll().isEmpty() || currencyUpdateService.isDataStale()) {
            currencyUpdateService.refreshRates();
        }
        return ResponseEntity.ok(sortedRates(currencyRateRepository.findAll()));
    }

    @PostMapping("/rates/refresh")
    public ResponseEntity<List<CurrencyRate>> refreshRates() {
        currencyUpdateService.refreshRates();
        return ResponseEntity.ok(sortedRates(currencyRateRepository.findAll()));
    }

    @GetMapping("/rates/{symbol}")
    public ResponseEntity<CurrencyRate> getRate(@PathVariable String symbol) {
        CurrencyRate rate = currencyRateRepository.findBySymbol(symbol.toUpperCase());
        if (rate == null && currencyRateRepository.findAll().isEmpty()) {
            currencyUpdateService.refreshRates();
            rate = currencyRateRepository.findBySymbol(symbol.toUpperCase());
        }
        return rate != null ? ResponseEntity.ok(rate) : ResponseEntity.notFound().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<PriceAlert>> getAlerts(@RequestParam String userId) {
        return ResponseEntity.ok(priceAlertRepository.findByUserId(userId));
    }

    @PostMapping("/alerts")
    public ResponseEntity<PriceAlert> createAlert(@RequestBody PriceAlert alert) {
        alert.setCreatedAt(LocalDateTime.now());
        alert.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(priceAlertRepository.save(alert));
    }

    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable String id) {
        priceAlertRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/alerts/{id}/toggle")
    public ResponseEntity<PriceAlert> toggleAlert(@PathVariable String id) {
        return priceAlertRepository.findById(id)
                .map(a -> {
                    a.setActive(!a.isActive());
                    return ResponseEntity.ok(priceAlertRepository.save(a));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private List<CurrencyRate> sortedRates(List<CurrencyRate> rates) {
        return rates.stream()
                .sorted(Comparator
                        .comparing((CurrencyRate r) -> "CRYPTO".equals(r.getType()))
                        .thenComparing(CurrencyRate::getSymbol))
                .toList();
    }
}
