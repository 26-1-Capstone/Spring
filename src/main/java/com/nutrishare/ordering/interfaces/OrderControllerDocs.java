package com.nutrishare.ordering.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.ordering.interfaces.OrderController.CreateOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Order", description = "주문 관련 API")
public interface OrderControllerDocs {

    @Operation(summary = "주문 생성", description = "상품을 주문합니다.")
    ApiResponse<Map<String, Object>> createOrder(
            Authentication authentication,
            @RequestBody @Valid CreateOrderRequest request);

    @Operation(summary = "주문 취소", description = "기존 주문을 취소합니다.")
    ApiResponse<Map<String, Object>> cancelOrder(
            Authentication authentication,
            @Parameter(description = "주문 ID") @PathVariable Long orderId);
}
