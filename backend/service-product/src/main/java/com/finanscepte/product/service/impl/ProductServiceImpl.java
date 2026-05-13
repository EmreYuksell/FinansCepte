package com.finanscepte.product.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.product.dto.ProductRequest;
import com.finanscepte.product.dto.ProductResponse;
import com.finanscepte.product.model.Product;
import com.finanscepte.product.repository.ProductRepository;
import com.finanscepte.product.service.ProductService;
import com.finanscepte.product.util.ProductMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public ProductResponse update(String id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setPrice(request.price());
        existing.setCategory(request.category());
        existing.setUpdatedAt(LocalDateTime.now());
        Product updated = productRepository.save(existing);
        return productMapper.toResponse(updated);
    }

    @Override
    public ProductResponse findById(String id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }
}
