package com.finanscepte.product.service.impl;

import com.finanscepte.common.AbstractGenericDtoService;
import com.finanscepte.common.GenericRepository;
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
public class ProductServiceImpl extends AbstractGenericDtoService<ProductRequest, ProductResponse, Product, String>
        implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    protected GenericRepository<Product, String> getRepository() {
        return productRepository;
    }

    @Override
    protected String getEntityName() {
        return "Product";
    }

    @Override
    protected Product toEntity(ProductRequest request) {
        return productMapper.toEntity(request);
    }

    @Override
    protected ProductResponse toResponse(Product entity) {
        return productMapper.toResponse(entity);
    }

    @Override
    protected void applyUpdate(Product entity, ProductRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setPrice(request.price());
        entity.setCategory(request.category());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .toList();
    }
}
