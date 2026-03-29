package com.nutrishare.catalog.application;

import com.nutrishare.catalog.domain.Product;
import com.nutrishare.catalog.infrastructure.persistence.ProductJpaRepository;
import com.nutrishare.common.exception.DomainException;
import com.nutrishare.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductJpaRepository productRepository;

    public Page<ProductSummaryView> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductSummaryView::from);
    }

    public Page<ProductSummaryView> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContaining(keyword, pageable)
                .map(ProductSummaryView::from);
    }

    public Page<ProductSummaryView> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(ProductSummaryView::from);
    }

    public ProductDetailView getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND) {
                    @Override
                    public String getMessage() {
                        return "Product not found";
                    }
                });

        return ProductDetailView.from(product);
    }

    public record ProductSummaryView(
            Long id,
            String name,
            Long price,
            String categoryName) {
        public static ProductSummaryView from(Product product) {
            return new ProductSummaryView(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getCategory().getName());
        }
    }

    public record ProductDetailView(
            Long id,
            String name,
            String description,
            Long price,
            Integer stockQuantity,
            Long categoryId,
            String categoryName) {
        public static ProductDetailView from(Product product) {
            return new ProductDetailView(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getCategory().getId(),
                    product.getCategory().getName());
        }
    }
}
