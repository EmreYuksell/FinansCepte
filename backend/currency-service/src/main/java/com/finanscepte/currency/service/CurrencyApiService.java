package com.finanscepte.currency.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyApiService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyApiService.class);

    private static final String[] BINANCE_SYMBOLS = {
            "BTCTRY", "ETHTRY", "XRPTRY", "SOLTRY", "BNBTRY", "ADATRY", "DOGETRY"
    };

    private final RestTemplate restTemplate;

    public CurrencyApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record FetchResult<T>(T data, boolean live, String source) {}

    public record CryptoRate(double price, double changePercent24h, double high24h, double low24h) {}

    public FetchResult<Map<String, Double>> fetchFiatRates() {
        FetchResult<Map<String, Double>> r = tryOpenErApi();
        if (r.live()) return r;
        r = tryExchangeRateApi();
        if (r.live()) return r;
        log.warn("All fiat providers failed; using fallback rates");
        return new FetchResult<>(fallbackFiat(), false, "FALLBACK");
    }

    public FetchResult<Map<String, CryptoRate>> fetchCryptoRates() {
        FetchResult<Map<String, CryptoRate>> r = tryBinanceBatch();
        if (r.live()) return r;
        for (String symbol : BINANCE_SYMBOLS) {
            Map<String, CryptoRate> single = fetchBinanceSingle(symbol);
            if (!single.isEmpty()) {
                log.info("Crypto rates loaded from Binance (single-symbol fallback)");
                return new FetchResult<>(single, true, "BINANCE");
            }
        }
        FetchResult<Map<String, CryptoRate>> cg = tryCoinGecko();
        if (cg.live()) return cg;
        log.warn("All crypto providers failed; using fallback rates");
        return new FetchResult<>(fallbackCrypto(), false, "FALLBACK");
    }

    @SuppressWarnings("unchecked")
    private FetchResult<Map<String, Double>> tryOpenErApi() {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                    "https://open.er-api.com/v6/latest/USD", Map.class);
            if (response == null || !"success".equals(response.get("result"))) {
                return emptyFiat("OPEN_ER_API");
            }
            Object ratesObj = response.get("rates");
            if (!(ratesObj instanceof Map<?, ?> fxRates)) {
                return emptyFiat("OPEN_ER_API");
            }
            Map<String, Double> rates = crossRatesFromUsdMap(fxRates);
            if (rates.isEmpty()) return emptyFiat("OPEN_ER_API");
            log.info("Fiat rates loaded from open.er-api.com ({} pairs)", rates.size());
            return new FetchResult<>(rates, true, "OPEN_ER_API");
        } catch (Exception e) {
            log.warn("open.er-api.com failed: {}", e.getMessage());
            return emptyFiat("OPEN_ER_API");
        }
    }

    @SuppressWarnings("unchecked")
    private FetchResult<Map<String, Double>> tryExchangeRateApi() {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                    "https://api.exchangerate-api.com/v4/latest/USD", Map.class);
            if (response == null || !(response.get("rates") instanceof Map<?, ?> fxRates)) {
                return emptyFiat("EXCHANGERATE_API");
            }
            Map<String, Double> rates = crossRatesFromUsdMap(fxRates);
            if (rates.isEmpty()) return emptyFiat("EXCHANGERATE_API");
            log.info("Fiat rates loaded from exchangerate-api.com");
            return new FetchResult<>(rates, true, "EXCHANGERATE_API");
        } catch (Exception e) {
            log.warn("exchangerate-api.com failed: {}", e.getMessage());
            return emptyFiat("EXCHANGERATE_API");
        }
    }

    private Map<String, Double> crossRatesFromUsdMap(Map<?, ?> fxRates) {
        Map<String, Double> rates = new LinkedHashMap<>();
        double tryPerUsd = toDouble(fxRates.get("TRY"));
        if (tryPerUsd <= 0) return rates;
        putCross(rates, "USD", tryPerUsd, fxRates);
        for (String sym : List.of("EUR", "GBP", "CHF", "JPY", "SAR", "AED")) {
            putCross(rates, sym, tryPerUsd, fxRates);
        }
        return rates;
    }

    private void putCross(Map<String, Double> rates, String symbol, double tryPerUsd, Map<?, ?> fxRates) {
        if ("USD".equals(symbol)) {
            rates.put(symbol, tryPerUsd);
            return;
        }
        double perUsd = toDouble(fxRates.get(symbol));
        if (perUsd > 0) rates.put(symbol, tryPerUsd / perUsd);
    }

    @SuppressWarnings("unchecked")
    private FetchResult<Map<String, CryptoRate>> tryBinanceBatch() {
        try {
            String symbolsJson = "[\"" + String.join("\",\"", BINANCE_SYMBOLS) + "\"]";
            // Tek encode: .encode() çağrısı Binance'ta 400 (illegal symbols) üretir
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.binance.com/api/v3/ticker/24hr")
                    .queryParam("symbols", symbolsJson)
                    .build()
                    .toUriString();
            List<Map<String, Object>> tickers = restTemplate.getForObject(url, List.class);
            Map<String, CryptoRate> rates = parseBinanceTickers(tickers);
            if (rates.isEmpty()) return new FetchResult<>(Map.of(), false, "BINANCE");
            log.info("Crypto rates loaded from Binance batch ({} pairs)", rates.size());
            return new FetchResult<>(rates, true, "BINANCE");
        } catch (Exception e) {
            log.warn("Binance batch failed: {}", e.getMessage());
            return new FetchResult<>(Map.of(), false, "BINANCE");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, CryptoRate> fetchBinanceSingle(String pair) {
        try {
            String url = "https://api.binance.com/api/v3/ticker/24hr?symbol=" + pair;
            Map<String, Object> ticker = restTemplate.getForObject(url, Map.class);
            if (ticker == null) return Map.of();
            return parseBinanceTickers(List.of(ticker));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Map<String, CryptoRate> parseBinanceTickers(List<Map<String, Object>> tickers) {
        Map<String, CryptoRate> rates = new LinkedHashMap<>();
        if (tickers == null) return rates;
        for (Map<String, Object> ticker : tickers) {
            String pair = stringVal(ticker.get("symbol"));
            if (pair == null || !pair.endsWith("TRY")) continue;
            String symbol = pair.substring(0, pair.length() - 3);
            double price = parseDouble(ticker.get("lastPrice"));
            if (price <= 0) continue;
            rates.put(symbol, new CryptoRate(
                    price,
                    parseDouble(ticker.get("priceChangePercent")),
                    parseDouble(ticker.get("highPrice")),
                    parseDouble(ticker.get("lowPrice"))
            ));
        }
        return rates;
    }

    @SuppressWarnings("unchecked")
    private FetchResult<Map<String, CryptoRate>> tryCoinGecko() {
        try {
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,ripple,solana,binancecoin"
                    + "&vs_currencies=try&include_24hr_change=true";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Map<String, CryptoRate> rates = new LinkedHashMap<>();
            if (response != null) {
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    if (!(entry.getValue() instanceof Map<?, ?> data)) continue;
                    String symbol = switch (entry.getKey()) {
                        case "bitcoin" -> "BTC";
                        case "ethereum" -> "ETH";
                        case "ripple" -> "XRP";
                        case "solana" -> "SOL";
                        case "binancecoin" -> "BNB";
                        default -> entry.getKey().toUpperCase();
                    };
                    double price = toDouble(data.get("try"));
                    if (price <= 0) continue;
                    double change = toDouble(data.get("try_24h_change"));
                    rates.put(symbol, new CryptoRate(price, change, price * 1.02, price * 0.98));
                }
            }
            if (rates.isEmpty()) return new FetchResult<>(Map.of(), false, "COINGECKO");
            log.info("Crypto rates loaded from CoinGecko");
            return new FetchResult<>(rates, true, "COINGECKO");
        } catch (Exception e) {
            log.warn("CoinGecko failed: {}", e.getMessage());
            return new FetchResult<>(Map.of(), false, "COINGECKO");
        }
    }

    private static FetchResult<Map<String, Double>> emptyFiat(String source) {
        return new FetchResult<>(Map.of(), false, source);
    }

    private static Map<String, Double> fallbackFiat() {
        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("USD", 38.50);
        rates.put("EUR", 41.20);
        rates.put("GBP", 48.90);
        rates.put("CHF", 43.10);
        rates.put("JPY", 0.26);
        rates.put("SAR", 10.25);
        rates.put("AED", 10.48);
        return rates;
    }

    private static Map<String, CryptoRate> fallbackCrypto() {
        Map<String, CryptoRate> rates = new LinkedHashMap<>();
        rates.put("BTC", new CryptoRate(3_200_000, 0, 3_250_000, 3_150_000));
        rates.put("ETH", new CryptoRate(120_000, 0, 122_000, 118_000));
        rates.put("XRP", new CryptoRate(35, 0, 36, 34));
        return rates;
    }

    private static double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s && !s.isBlank()) return Double.parseDouble(s);
        return 0;
    }

    private static double parseDouble(Object val) {
        return toDouble(val);
    }

    private static String stringVal(Object val) {
        return val == null ? null : val.toString();
    }
}
