package com.nutrishare.cart.interfaces;

import com.nutrishare.cart.application.CartCommandService;
import com.nutrishare.cart.application.CartQueryService;
import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.infrastructure.security.AuthUserExtractor;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController implements CartControllerDocs {

    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;

    @PostMapping
    public ApiResponse<Map<String, Object>> addToCart(
            Authentication authentication,
            @RequestBody @Valid AddToCartRequest request) {
        Long userId = AuthUserExtractor.getUserId(authentication);

        cartCommandService.addItemToCart(userId, request.productId(), request.quantity());

        return ApiResponse.success(Map.of(
                "status", "ACCEPTED"));
    }

    @GetMapping
    public ApiResponse<CartQueryService.CartView> getCart(
            Authentication authentication) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        return ApiResponse.success(cartQueryService.getCart(userId));
    }

    @PutMapping("/{productId}")
    public ApiResponse<Map<String, Object>> updateItem(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateItemRequest request) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        cartCommandService.updateItemQuantity(userId, productId, request.quantity());
        return ApiResponse.success(Map.of("status", "UPDATED", "productId", productId));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Map<String, Object>> removeItem(
            Authentication authentication,
            @PathVariable Long productId) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        cartCommandService.removeItemFromCart(userId, productId);
        return ApiResponse.success(Map.of("status", "DELETED", "productId", productId));
    }

    public record AddToCartRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity) {
    }

    public record UpdateItemRequest(
            @NotNull @Min(0) Integer quantity) {
    }
}
