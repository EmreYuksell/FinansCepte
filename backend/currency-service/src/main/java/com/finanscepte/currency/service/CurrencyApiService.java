package com.finanscepte.currency.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CurrencyApiService {

    private final RestTemplate restTemplate;

    public CurrencyApiService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Double> fetchFiatRates() {
        Map<String, Double> rates = new LinkedHashMap<>();
        try {
            String url = "https://api.exchangerate-api.com/v4/latest/USD";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("rates")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> fxRates = (Map<String, Object>) response.get("rates");
                double tryRate = getDouble(fxRates, "TRY");
                double eurRate = getDouble(fxRates, "EUR");
                double gbpRate = getDouble(fxRates, "GBP");

                rates.put("USD", tryRate);
                rates.put("EUR", tryRate / eurRate);
                rates.put("GBP", tryRate / gbpRate);
            }
        } catch (Exception e) {
            rates.put("USD", 32.45);
            rates.put("EUR", 35.12);
            rates.put("GBP", 40.80);
        }
        return rates;
    }

    public Map<String, CryptoRate> fetchCryptoRates() {
        Map<String, CryptoRate> rates = new LinkedHashMap<>();
        try {
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,ripple&vs_currencies=try&include_24hr_change=true&include_24hr_high_low=true";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    String id = entry.getKey();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) entry.getValue();
                    String symbol = id.equals("ripple") ? "XRP" : id.equals("bitcoin") ? "BTC" : "ETH";
                    rates.put(symbol, new CryptoRate(
                            getDouble(data, "try"),
                            getDouble(data, "try_24h_change"),
                            getDouble(data, "try_24h_high"),
                            getDouble(data, "try_24h_low")
                    ));
                }
            }
        } catch (Exception e) {
            rates.put("BTC", new CryptoRate(2145000, 2.4, 2200000, 2100000));
            rates.put("ETH", new CryptoRate(125400, 1.8, 128000, 122000));
            rates.put("XRP", new CryptoRate(18.50, -0.3, 19.0, 18.0));
        }
        return rates;
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) return Double.parseDouble(s);
        return 0;
    }

    public record CryptoRate(double price, double changePercent24h, double high24h, double low24h) {}
}
