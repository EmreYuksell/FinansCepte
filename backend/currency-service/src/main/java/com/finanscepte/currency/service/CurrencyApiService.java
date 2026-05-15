package com.finanscepte.currency.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyApiService {

    private static final String FIAT_URL = "https://open.er-api.com/v6/latest/USD";
    private static final String BINANCE_TICKERS =
            "[\"BTCTRY\",\"ETHTRY\",\"XRPTRY\",\"SOLTRY\",\"BNBTRY\",\"ADATRY\",\"DOGETRY\"]";
    private static final String COINGECKO_URL =
            "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,ripple,solana,binancecoin"
                    + "&vs_currencies=try&include_24hr_change=true";

    private final RestTemplate restTemplate;

    public CurrencyApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> fetchFiatRates() {
        Map<String, Double> rates = new LinkedHashMap<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(FIAT_URL, Map.class);
            if (response != null && "success".equals(response.get("result"))
                    && response.get("rates") instanceof Map<?, ?> fxRates) {
                double tryPerUsd = toDouble(fxRates.get("TRY"));
                if (tryPerUsd > 0) {
                    putCrossRate(rates, "USD", tryPerUsd, fxRates);
                    putCrossRate(rates, "EUR", tryPerUsd, fxRates);
                    putCrossRate(rates, "GBP", tryPerUsd, fxRates);
                    putCrossRate(rates, "CHF", tryPerUsd, fxRates);
                    putCrossRate(rates, "JPY", tryPerUsd, fxRates);
                    putCrossRate(rates, "SAR", tryPerUsd, fxRates);
                    putCrossRate(rates, "AED", tryPerUsd, fxRates);
                }
            }
        } catch (Exception ignored) {
        }
        if (rates.isEmpty()) {
            rates.put("USD", 38.50);
            rates.put("EUR", 41.20);
            rates.put("GBP", 48.90);
            rates.put("CHF", 43.10);
            rates.put("JPY", 0.26);
            rates.put("SAR", 10.25);
            rates.put("AED", 10.48);
        }
        return rates;
    }

    private void putCrossRate(Map<String, Double> rates, String symbol, double tryPerUsd, Map<?, ?> fxRates) {
        if ("USD".equals(symbol)) {
            rates.put(symbol, tryPerUsd);
            return;
        }
        double perUsd = toDouble(fxRates.get(symbol));
        if (perUsd > 0) {
            rates.put(symbol, tryPerUsd / perUsd);
        }
    }

    public Map<String, CryptoRate> fetchCryptoRates() {
        Map<String, CryptoRate> fromBinance = fetchCryptoFromBinance();
        if (!fromBinance.isEmpty()) {
            return fromBinance;
        }
        return fetchCryptoFromCoinGecko();
    }

    @SuppressWarnings("unchecked")
    private Map<String, CryptoRate> fetchCryptoFromBinance() {
        Map<String, CryptoRate> rates = new LinkedHashMap<>();
        try {
            String encoded = URLEncoder.encode(BINANCE_TICKERS, StandardCharsets.UTF_8);
            String url = "https://api.binance.com/api/v3/ticker/24hr?symbols=" + encoded;
            List<Map<String, Object>> tickers = restTemplate.getForObject(url, List.class);
            if (tickers == null) {
                return rates;
            }
            for (Map<String, Object> ticker : tickers) {
                String pair = stringVal(ticker.get("symbol"));
                if (pair == null || !pair.endsWith("TRY")) {
                    continue;
                }
                String symbol = pair.substring(0, pair.length() - 3);
                double price = parseDouble(ticker.get("lastPrice"));
                if (price <= 0) {
                    continue;
                }
                rates.put(symbol, new CryptoRate(
                        price,
                        parseDouble(ticker.get("priceChangePercent")),
                        parseDouble(ticker.get("highPrice")),
                        parseDouble(ticker.get("lowPrice"))
                ));
            }
        } catch (Exception ignored) {
        }
        return rates;
    }

    @SuppressWarnings("unchecked")
    private Map<String, CryptoRate> fetchCryptoFromCoinGecko() {
        Map<String, CryptoRate> rates = new LinkedHashMap<>();
        try {
            Map<String, Object> response = restTemplate.getForObject(COINGECKO_URL, Map.class);
            if (response == null) {
                return rates;
            }
            for (Map.Entry<String, Object> entry : response.entrySet()) {
                String id = entry.getKey();
                if (!(entry.getValue() instanceof Map<?, ?> data)) {
                    continue;
                }
                String symbol = switch (id) {
                    case "bitcoin" -> "BTC";
                    case "ethereum" -> "ETH";
                    case "ripple" -> "XRP";
                    case "solana" -> "SOL";
                    case "binancecoin" -> "BNB";
                    default -> id.toUpperCase();
                };
                double price = toDouble(data.get("try"));
                if (price <= 0) {
                    continue;
                }
                double change = toDouble(data.get("try_24h_change"));
                rates.put(symbol, new CryptoRate(price, change, price * 1.02, price * 0.98));
            }
        } catch (Exception ignored) {
        }
        if (rates.isEmpty()) {
            rates.put("BTC", new CryptoRate(3_200_000, 0, 3_250_000, 3_150_000));
            rates.put("ETH", new CryptoRate(120_000, 0, 122_000, 118_000));
            rates.put("XRP", new CryptoRate(35, 0, 36, 34));
        }
        return rates;
    }

    private static double toDouble(Object val) {
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        if (val instanceof String s && !s.isBlank()) {
            return Double.parseDouble(s);
        }
        return 0;
    }

    private static double parseDouble(Object val) {
        return toDouble(val);
    }

    private static String stringVal(Object val) {
        return val == null ? null : val.toString();
    }

    public record CryptoRate(double price, double changePercent24h, double high24h, double low24h) {}
}
