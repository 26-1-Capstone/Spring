package com.nutrishare.iam.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.iam.application.MyPageQueryService;
import com.nutrishare.iam.interfaces.UserProfileController.ReviewUpsertRequest;
import com.nutrishare.iam.interfaces.UserProfileController.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "User Profile", description = "마이페이지 (프로필/히스토리) API")
public interface UserProfileControllerDocs {

        @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필(닉네임, 주소)을 조회합니다.")
        ApiResponse<MyPageQueryService.UserProfileView> getProfile(
                        @Parameter(hidden = true) Authentication authentication);

        @Operation(summary = "내 프로필 수정", description = "닉네임 및 주소 정보를 수정합니다.")
        ApiResponse<Map<String, Object>> updateProfile(
                        @Parameter(hidden = true) Authentication authentication,
                        @RequestBody @Valid UpdateProfileRequest request);

        @Operation(summary = "주문 내역 조회", description = "나의 주문 내역 목록을 조회합니다.")
        ApiResponse<List<MyPageQueryService.OrderSummaryView>> getOrderHistory(
                        @Parameter(hidden = true) Authentication authentication);

        @Operation(summary = "공동구매 참여 내역 조회", description = "나의 공동구매 참여 내역을 조회합니다.")
        ApiResponse<List<MyPageQueryService.ParticipationSummaryView>> getParticipationHistory(
                        @Parameter(hidden = true) Authentication authentication);

        @Operation(summary = "리뷰 작성/수정", description = "완료된 공동구매에 대한 별점 + 한줄 리뷰를 저장합니다.")
        ApiResponse<Map<String, Object>> upsertReview(
                        @Parameter(hidden = true) Authentication authentication,
                        @RequestBody @Valid ReviewUpsertRequest request);
}
