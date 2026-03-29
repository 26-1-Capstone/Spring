package com.nutrishare.catalog.domain;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(Long id);
    // Basic search methods could go here
}
