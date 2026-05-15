package com.finanscepte.currency.controller;

import com.finanscepte.currency.model.CurrencyRate;
import com.finanscepte.currency.model.PriceAlert;
import com.finanscepte.currency.repository.CurrencyRateRepository;
import com.finanscepte.currency.repository.PriceAlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CurrencyRateRepository currencyRateRepository;
    @MockBean private PriceAlertRepository priceAlertRepository;

    @Test
    void getRates_shouldReturnList() throws Exception {
        CurrencyRate rate = CurrencyRate.builder().symbol("USD").name("Dolar").rate(32.5).build();
        when(currencyRateRepository.findAll()).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/currency/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("USD"));
    }

    @Test
    void getRate_shouldReturnNotFound_whenMissing() throws Exception {
        when(currencyRateRepository.findBySymbol("XXX")).thenReturn(null);

        mockMvc.perform(get("/api/currency/rates/XXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAlerts_shouldReturnByUser() throws Exception {
        PriceAlert alert = PriceAlert.builder().userId("u1").symbol("BTC").targetPrice(50000).build();
        when(priceAlertRepository.findByUserId("u1")).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/currency/alerts").param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC"));
    }

    @Test
    void createAlert_shouldReturnCreated() throws Exception {
        PriceAlert saved = PriceAlert.builder().id("1").userId("u1").symbol("ETH").targetPrice(3000).isActive(true).build();
        when(priceAlertRepository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/currency/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"symbol\":\"ETH\",\"targetPrice\":3000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("ETH"));
    }

    @Test
    void toggleAlert_shouldReturnOk() throws Exception {
        PriceAlert alert = PriceAlert.builder().id("1").isActive(true).build();
        when(priceAlertRepository.findById("1")).thenReturn(Optional.of(alert));
        when(priceAlertRepository.save(any())).thenReturn(alert);

        mockMvc.perform(put("/api/currency/alerts/1/toggle"))
                .andExpect(status().isOk());
    }
}
