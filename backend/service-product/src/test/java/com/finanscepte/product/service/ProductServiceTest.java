package com.finanscepte.product.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.product.dto.ProductRequest;
import com.finanscepte.product.dto.ProductResponse;
import com.finanscepte.product.model.Product;
import com.finanscepte.product.repository.ProductRepository;
import com.finanscepte.product.service.impl.ProductServiceImpl;
import com.finanscepte.product.util.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest request;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("1").name("Test").description("Desc")
                .price(BigDecimal.TEN).category("kat").build();

        request = new ProductRequest("Test", "Desc", BigDecimal.TEN, "kat");

        response = new ProductResponse("1", "Test", "Desc", BigDecimal.TEN, "kat", null, null);
    }

    @Test
    void create_shouldSaveAndReturnProduct() {
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertThat(result.name()).isEqualTo("Test");
        verify(productRepository).save(product);
    }

    @Test
    void findAll_shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test");
    }

    @Test
    void findById_shouldReturnProduct_whenExists() {
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.findById("1");

        assertThat(result.id()).isEqualTo("1");
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(productRepository.findById("99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById("99"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    void update_shouldUpdateExistingProduct() {
        Product updated = Product.builder().id("1").name("Updated").price(BigDecimal.ONE).build();
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(updated);
        when(productMapper.toResponse(any())).thenReturn(new ProductResponse("1", "Updated", null, BigDecimal.ONE, null, null, null));

        ProductResponse result = productService.update("1", new ProductRequest("Updated", null, BigDecimal.ONE, null));

        assertThat(result.name()).isEqualTo("Updated");
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(productRepository.existsById("99")).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteById("99"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByCategory_shouldReturnFiltered() {
        when(productRepository.findByCategory("kat")).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.findByCategory("kat");

        assertThat(result).hasSize(1);
        verify(productRepository).findByCategory("kat");
    }
}
