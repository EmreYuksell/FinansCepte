package com.finanscepte.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.product.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(delete("/api/products/999")).andReturn();
    }

    @Test
    void fullCrudFlow_shouldWorkEndToEnd() throws Exception {
        // CREATE
        ProductRequest req = new ProductRequest("E2E Test", "End-to-end", BigDecimal.valueOf(150), "test");
        String postResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("E2E Test"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(postResult).get("id").asText();

        // READ
        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("E2E Test"));

        // UPDATE
        ProductRequest update = new ProductRequest("Updated", "New desc", BigDecimal.ONE, "newcat");
        mockMvc.perform(put("/api/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.category").value("newcat"));

        // DELETE
        mockMvc.perform(delete("/api/products/" + id))
                .andExpect(status().isNoContent());

        // VERIFY DELETED
        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNonExistent_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createInvalid_shouldReturn400() throws Exception {
        String invalid = "{\"name\":\"\",\"price\":null,\"category\":\"\"}";
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }
}
