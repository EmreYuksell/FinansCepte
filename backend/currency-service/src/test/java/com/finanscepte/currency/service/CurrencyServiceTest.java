package com.finanscepte.currency.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock private RestTemplate restTemplate;
    private CurrencyApiService service;

    @BeforeEach
    void setUp() {
        service = new CurrencyApiService(restTemplate);
    }

    @Test
    void fetchFiatRates_shouldUseFallback_whenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("offline"));

        CurrencyApiService.FetchResult<Map<String, Double>> result = service.fetchFiatRates();

        assertThat(result.live()).isFalse();
        assertThat(result.source()).isEqualTo("FALLBACK");
        assertThat(result.data()).containsKeys("USD", "EUR", "GBP");
        assertThat(result.data().get("USD")).isEqualTo(38.50);
    }

    @Test
    void fetchCryptoRates_shouldUseFallback_whenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("offline"));
        when(restTemplate.getForObject(contains("symbol="), eq(Map.class))).thenThrow(new RuntimeException("offline"));

        CurrencyApiService.FetchResult<Map<String, CurrencyApiService.CryptoRate>> result = service.fetchCryptoRates();

        assertThat(result.live()).isFalse();
        assertThat(result.data()).containsKeys("BTC", "ETH", "XRP");
    }
}
