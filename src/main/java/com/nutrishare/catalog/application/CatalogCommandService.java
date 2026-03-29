package com.nutrishare.catalog.application;

import com.nutrishare.catalog.domain.Category;
import com.nutrishare.catalog.domain.CategoryRepository;
import com.nutrishare.catalog.domain.Product;
import com.nutrishare.catalog.domain.ProductRepository;
import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogCommandService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Long createCategory(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new DomainException(ErrorCode.INVALID_REQUEST) {
                @Override
                public String getMessage() {
                    return "Category already exists";
                }
            };
        }
        Category category = new Category(name);
        return categoryRepository.save(category).getId();
    }

    public Long createProduct(Long categoryId, String name, String description, Long price, Integer stock) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Category not found";
                    }
                });

        Product product = Product.create(category, name, description, price, stock);
        return productRepository.save(product).getId();
    }
}
