package com.nutrishare.catalog.interfaces;

import com.nutrishare.catalog.interfaces.ProductController.CreateProductRequest;
import com.nutrishare.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductControllerDocs {

    @Operation(summary = "상품 생성", description = "관리자가 새로운 상품을 등록합니다.")
    ApiResponse<Map<String, Object>> createProduct(@RequestBody @Valid CreateProductRequest request);

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 페이징하여 조회합니다.")
    ApiResponse<org.springframework.data.domain.Page<com.nutrishare.catalog.application.ProductQueryService.ProductSummaryView>> getProducts(
            org.springframework.data.domain.Pageable pageable);

    @Operation(summary = "상품 검색", description = "상품명으로 상품을 검색합니다.")
    ApiResponse<org.springframework.data.domain.Page<com.nutrishare.catalog.application.ProductQueryService.ProductSummaryView>> searchProducts(
            @io.swagger.v3.oas.annotations.Parameter(description = "검색어", example = "사과") @org.springframework.web.bind.annotation.RequestParam String q,
            org.springframework.data.domain.Pageable pageable);

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    ApiResponse<com.nutrishare.catalog.application.ProductQueryService.ProductDetailView> getProductDetail(
            @io.swagger.v3.oas.annotations.Parameter(description = "상품 ID", example = "1") @org.springframework.web.bind.annotation.PathVariable Long id);
}
