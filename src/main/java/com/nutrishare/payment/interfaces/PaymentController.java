package com.nutrishare.payment.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.infrastructure.security.AuthUserExtractor;
import com.nutrishare.payment.application.PaymentCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {

        private final PaymentCommandService paymentCommandService;

        @PostMapping("/confirm")
        public ApiResponse<Map<String, Object>> confirmPayment(
                        Authentication authentication,
                        @RequestBody @Valid PaymentConfirmRequest request) {
                Long userId = AuthUserExtractor.getUserId(authentication);

                Long paymentId = paymentCommandService.processPayment(
                                userId,
                                request.orderId(),
                                request.amount(),
                                request.paymentProvider(),
                                request.providerPaymentKey());

                return ApiResponse.success(Map.of(
                                "resourceId", paymentId,
                                "status", "ACCEPTED"));
        }

        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 승인 요청")
        public record PaymentConfirmRequest(
                        @io.swagger.v3.oas.annotations.media.Schema(description = "주문 ID", example = "1") @NotNull Long orderId,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 금액", example = "15000") @NotNull Long amount,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "결제 제공자", example = "TOSS") @NotBlank String paymentProvider,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "제공자 결제 키", example = "paymentKeys_...") @NotBlank String providerPaymentKey) {
        }
}
