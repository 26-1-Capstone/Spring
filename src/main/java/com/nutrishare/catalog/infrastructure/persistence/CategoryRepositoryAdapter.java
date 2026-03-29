package com.nutrishare.catalog.infrastructure.persistence;

import com.nutrishare.catalog.domain.Category;
import com.nutrishare.catalog.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return jpaRepository.findByName(name);
    }
}
