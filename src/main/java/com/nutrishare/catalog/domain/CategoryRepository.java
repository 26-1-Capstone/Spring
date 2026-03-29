package com.nutrishare.catalog.domain;

import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);
}
