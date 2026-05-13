package com.finanscepte.product.repository;

import com.finanscepte.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void save_shouldPersistProduct() {
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .category("elektronik")
                .build();

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Product");
    }

    @Test
    void findByCategory_shouldReturnMatchingProducts() {
        Product p1 = Product.builder().name("P1").category("A").price(BigDecimal.TEN).build();
        Product p2 = Product.builder().name("P2").category("A").price(BigDecimal.ONE).build();
        Product p3 = Product.builder().name("P3").category("B").price(BigDecimal.TEN).build();

        productRepository.saveAll(List.of(p1, p2, p3));

        List<Product> result = productRepository.findByCategory("A");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("P1", "P2");
    }

    @Test
    void findById_shouldReturnProduct_whenExists() {
        Product saved = productRepository.save(
                Product.builder().name("Test").category("X").price(BigDecimal.TEN).build());

        Optional<Product> result = productRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test");
    }

    @Test
    void delete_shouldRemoveProduct() {
        Product saved = productRepository.save(
                Product.builder().name("Test").category("X").price(BigDecimal.TEN).build());

        productRepository.deleteById(saved.getId());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }
}
