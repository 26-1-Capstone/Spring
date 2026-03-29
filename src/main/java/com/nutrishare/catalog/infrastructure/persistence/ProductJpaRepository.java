package com.nutrishare.catalog.infrastructure.persistence;

import com.nutrishare.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    org.springframework.data.domain.Page<Product> findByNameContaining(String name,
            org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Product> findByCategoryId(Long categoryId,
            org.springframework.data.domain.Pageable pageable);
}
