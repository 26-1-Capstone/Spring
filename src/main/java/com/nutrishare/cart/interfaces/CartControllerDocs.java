package com.nutrishare.cart.interfaces;

import com.nutrishare.cart.interfaces.CartController.AddToCartRequest;
import com.nutrishare.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Cart", description = "장바구니 관련 API")
public interface CartControllerDocs {

        @Operation(summary = "장바구니 담기", description = "상품을 장바구니에 추가합니다.")
        ApiResponse<Map<String, Object>> addToCart(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) org.springframework.security.core.Authentication authentication,
                        @RequestBody @Valid AddToCartRequest request);

        @Operation(summary = "장바구니 조회", description = "장바구니에 담긴 상품 목록을 조회합니다.")
        ApiResponse<com.nutrishare.cart.application.CartQueryService.CartView> getCart(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) org.springframework.security.core.Authentication authentication);

        @Operation(summary = "장바구니 수량 수정", description = "장바구니에 담긴 상품의 수량을 수정합니다.")
        ApiResponse<Map<String, Object>> updateItem(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) org.springframework.security.core.Authentication authentication,
                        @io.swagger.v3.oas.annotations.Parameter(description = "상품 ID", example = "1") @org.springframework.web.bind.annotation.PathVariable Long productId,
                        @RequestBody @Valid com.nutrishare.cart.interfaces.CartController.UpdateItemRequest request);

        @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.")
        ApiResponse<Map<String, Object>> removeItem(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) org.springframework.security.core.Authentication authentication,
                        @io.swagger.v3.oas.annotations.Parameter(description = "상품 ID", example = "1") @org.springframework.web.bind.annotation.PathVariable Long productId);
}
