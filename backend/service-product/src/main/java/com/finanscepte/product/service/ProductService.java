package com.finanscepte.product.service;

import com.finanscepte.product.dto.ProductRequest;
import com.finanscepte.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse update(String id, ProductRequest request);

    ProductResponse findById(String id);

    List<ProductResponse> findAll();

    List<ProductResponse> findByCategory(String category);

    void deleteById(String id);
}
