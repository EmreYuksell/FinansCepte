package com.finanscepte.product.controller;

import com.finanscepte.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanscepte.product.dto.ProductRequest;
import com.finanscepte.product.dto.ProductResponse;
import com.finanscepte.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void findAll_shouldReturnProductList() throws Exception {
        ProductResponse response = new ProductResponse("1", "Test", "Desc", BigDecimal.TEN, "kat", LocalDateTime.now(), LocalDateTime.now());
        when(productService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].category").value("kat"));
    }

    @Test
    void findById_shouldReturnProduct() throws Exception {
        ProductResponse response = new ProductResponse("1", "Test", "Desc", BigDecimal.TEN, "kat", LocalDateTime.now(), LocalDateTime.now());
        when(productService.findById("1")).thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        ProductRequest request = new ProductRequest("Test", "Desc", BigDecimal.TEN, "kat");
        ProductResponse response = new ProductResponse("1", "Test", "Desc", BigDecimal.TEN, "kat", LocalDateTime.now(), LocalDateTime.now());
        when(productService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void create_shouldReturnBadRequest_whenInvalid() throws Exception {
        ProductRequest request = new ProductRequest("", null, null, "");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        ProductRequest request = new ProductRequest("Updated", "Desc", BigDecimal.ONE, "yeni");
        ProductResponse response = new ProductResponse("1", "Updated", "Desc", BigDecimal.ONE, "yeni", LocalDateTime.now(), LocalDateTime.now());
        when(productService.update(eq("1"), any())).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findByCategory_shouldReturnFiltered() throws Exception {
        ProductResponse response = new ProductResponse("1", "Test", "Desc", BigDecimal.TEN, "teknoloji", LocalDateTime.now(), LocalDateTime.now());
        when(productService.findByCategory("teknoloji")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/category/teknoloji"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("teknoloji"));
    }
}
