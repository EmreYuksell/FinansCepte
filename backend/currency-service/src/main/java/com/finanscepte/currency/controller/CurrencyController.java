package com.finanscepte.currency.controller;

import com.finanscepte.currency.model.CurrencyRate;
import com.finanscepte.currency.model.PriceAlert;
import com.finanscepte.currency.repository.CurrencyRateRepository;
import com.finanscepte.currency.repository.PriceAlertRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyRateRepository currencyRateRepository;
    private final PriceAlertRepository priceAlertRepository;

    public CurrencyController(CurrencyRateRepository currencyRateRepository, PriceAlertRepository priceAlertRepository) {
        this.currencyRateRepository = currencyRateRepository;
        this.priceAlertRepository = priceAlertRepository;
    }

    @GetMapping("/rates")
    public ResponseEntity<List<CurrencyRate>> getRates() {
        return ResponseEntity.ok(currencyRateRepository.findAll());
    }

    @GetMapping("/rates/{symbol}")
    public ResponseEntity<CurrencyRate> getRate(@PathVariable String symbol) {
        CurrencyRate rate = currencyRateRepository.findBySymbol(symbol);
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
                .map(a -> { a.setActive(!a.isActive()); return ResponseEntity.ok(priceAlertRepository.save(a)); })
                .orElse(ResponseEntity.notFound().build());
    }
}
