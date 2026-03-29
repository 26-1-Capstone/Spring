package com.nutrishare.iam.interfaces;

import com.nutrishare.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 Access Token을 재발급합니다.")
    ApiResponse<String> reissue(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response);
}
