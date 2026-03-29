package com.nutrishare.catalog.interfaces;

import com.nutrishare.catalog.application.CatalogCommandService;
import com.nutrishare.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.nutrishare.catalog.application.ProductQueryService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {

        private final CatalogCommandService catalogCommandService;
        private final ProductQueryService productQueryService;

        // Admin only in real world
        @PostMapping
        public ApiResponse<Map<String, Object>> createProduct(@RequestBody @Valid CreateProductRequest request) {
                Long productId = catalogCommandService.createProduct(
                                request.categoryId(),
                                request.name(),
                                request.description(),
                                request.price(),
                                request.stockQuantity());

                return ApiResponse.success(Map.of(
                                "resourceId", productId,
                                "status", "CREATED"));
        }

        @GetMapping
        public ApiResponse<org.springframework.data.domain.Page<ProductQueryService.ProductSummaryView>> getProducts(
                        org.springframework.data.domain.Pageable pageable) {
                return ApiResponse.success(productQueryService.getProducts(pageable));
        }

        @GetMapping("/search")
        public ApiResponse<org.springframework.data.domain.Page<ProductQueryService.ProductSummaryView>> searchProducts(
                        @RequestParam String q,
                        org.springframework.data.domain.Pageable pageable) {
                return ApiResponse.success(productQueryService.searchProducts(q, pageable));
        }

        @GetMapping("/{id}")
        public ApiResponse<ProductQueryService.ProductDetailView> getProductDetail(@PathVariable Long id) {
                return ApiResponse.success(productQueryService.getProductDetail(id));
        }

        public record CreateProductRequest(
                        @NotNull Long categoryId,
                        @NotBlank String name,
                        String description,
                        @NotNull @Min(0) Long price,
                        @NotNull @Min(0) Integer stockQuantity) {
        }
}
