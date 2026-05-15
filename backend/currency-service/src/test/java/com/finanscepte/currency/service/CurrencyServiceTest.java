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
        service = new CurrencyApiService();
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    @Test
    void fetchFiatRates_shouldUseFallback_whenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("offline"));

        Map<String, Double> rates = service.fetchFiatRates();

        assertThat(rates).containsKeys("USD", "EUR", "GBP");
        assertThat(rates.get("USD")).isEqualTo(38.50);
    }

    @Test
    void fetchCryptoRates_shouldUseFallback_whenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("offline"));

        Map<String, CurrencyApiService.CryptoRate> rates = service.fetchCryptoRates();

        assertThat(rates).containsKeys("BTC", "ETH", "XRP");
    }
}
