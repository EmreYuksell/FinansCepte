package com.finanscepte.product.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.product.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends GenericRepository<Product, String> {

    List<Product> findByCategory(String category);
}
