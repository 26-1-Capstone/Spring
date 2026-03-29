package com.nutrishare.ordering.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.infrastructure.security.AuthUserExtractor;
import com.nutrishare.ordering.application.OrderCommandService;
import com.nutrishare.ordering.domain.OrderItem;
import com.nutrishare.ordering.domain.ShippingAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

        private final OrderCommandService orderCommandService;

        @PostMapping
        public ApiResponse<Map<String, Object>> createOrder(
                        Authentication authentication,
                        @RequestBody @Valid CreateOrderRequest request) {
                Long userId = AuthUserExtractor.getUserId(authentication);

                ShippingAddress address = new ShippingAddress(
                                request.shippingAddress.zipCode,
                                request.shippingAddress.line1,
                                request.shippingAddress.line2);

                List<OrderItem> items = request.items.stream()
                                .map(i -> new OrderItem(i.productId, i.productName, i.unitPrice, i.quantity))
                                .collect(Collectors.toList());

                Long orderId = orderCommandService.createOrder(userId, address, items);

                return ApiResponse.success(Map.of(
                                "resourceId", orderId,
                                "status", "ACCEPTED"));
        }

        @PostMapping("/{orderId}/cancel")
        public ApiResponse<Map<String, Object>> cancelOrder(
                        Authentication authentication,
                        @PathVariable Long orderId) {
                Long userId = AuthUserExtractor.getUserId(authentication);

                orderCommandService.cancelOrder(userId, orderId);

                return ApiResponse.success(Map.of(
                                "resourceId", orderId,
                                "status", "CANCELED"));
        }

        @io.swagger.v3.oas.annotations.media.Schema(description = "주문 생성 요청")
        public record CreateOrderRequest(
                        @io.swagger.v3.oas.annotations.media.Schema(description = "배송지 정보") @NotNull AddressRequest shippingAddress,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "주문 상품 목록") @NotEmpty List<ItemRequest> items) {
        }

        @io.swagger.v3.oas.annotations.media.Schema(description = "배송지 정보")
        public record AddressRequest(
                        @io.swagger.v3.oas.annotations.media.Schema(description = "우편번호", example = "12345") String zipCode,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "기본 주소", example = "서울시 강남구") String line1,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "상세 주소", example = "101호") String line2) {
        }

        @io.swagger.v3.oas.annotations.media.Schema(description = "주문 상품 정보")
        public record ItemRequest(
                        @io.swagger.v3.oas.annotations.media.Schema(description = "상품 ID", example = "1") Long productId,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "상품명", example = "유기농 사과") String productName,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "단가", example = "15000") Long unitPrice,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "수량", example = "2") Integer quantity) {
        }
}
