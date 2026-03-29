package com.nutrishare.payment.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.payment.interfaces.PaymentController.PaymentConfirmRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Payment", description = "결제 관련 API")
public interface PaymentControllerDocs {

    @Operation(summary = "결제 승인", description = "결제를 승인하고 처리합니다.")
    ApiResponse<Map<String, Object>> confirmPayment(Authentication authentication,
            @RequestBody @Valid PaymentConfirmRequest request);
}
