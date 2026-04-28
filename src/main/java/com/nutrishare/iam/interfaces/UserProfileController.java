package com.nutrishare.iam.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.iam.application.MyPageQueryService;
import com.nutrishare.iam.application.UserProfileCommandService;
import com.nutrishare.iam.domain.Address;
import com.nutrishare.infrastructure.security.AuthUserExtractor;
import com.nutrishare.review.application.ReviewCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController implements UserProfileControllerDocs {

    private final MyPageQueryService myPageQueryService;
    private final UserProfileCommandService userProfileCommandService;
    private final ReviewCommandService reviewCommandService;

    @GetMapping
    public ApiResponse<MyPageQueryService.UserProfileView> getProfile(Authentication authentication) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        return ApiResponse.success(myPageQueryService.getUserProfile(userId));
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> updateProfile(
            Authentication authentication,
            @RequestBody @Valid UpdateProfileRequest request) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        Address address = Address.of(request.zipCode, request.addressLine1, request.addressLine2, request.dong);

        userProfileCommandService.updateProfile(userId, request.nickname, address);

        return ApiResponse.success(Map.of("status", "UPDATED", "userId", userId));
    }

    @DeleteMapping
    public ApiResponse<Map<String, Object>> withdraw(Authentication authentication) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        userProfileCommandService.withdraw(userId);
        SecurityContextHolder.clearContext();
        return ApiResponse.success(Map.of("status", "WITHDRAWN", "userId", userId));
    }

    @GetMapping("/orders")
    public ApiResponse<List<MyPageQueryService.OrderSummaryView>> getOrderHistory(
            Authentication authentication) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        return ApiResponse.success(myPageQueryService.getOrderHistory(userId));
    }

    @GetMapping("/participations")
    public ApiResponse<List<MyPageQueryService.ParticipationSummaryView>> getParticipationHistory(
            Authentication authentication) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        return ApiResponse.success(myPageQueryService.getParticipationHistory(userId));
    }

    @PostMapping("/reviews")
    public ApiResponse<Map<String, Object>> upsertReview(
            Authentication authentication,
            @RequestBody @Valid ReviewUpsertRequest request) {
        Long userId = AuthUserExtractor.getUserId(authentication);
        Long reviewId = reviewCommandService.upsertReview(
                userId,
                request.participationId,
                request.rating,
                request.comment);

        return ApiResponse.success(Map.of("status", "REVIEW_SAVED", "resourceId", reviewId));
    }

    public record UpdateProfileRequest(
            @NotBlank String nickname,
            String zipCode,
            String addressLine1,
            String addressLine2,
            @Size(max = 30) String dong) {
    }

    public record ReviewUpsertRequest(
            @Min(1) Long participationId,
            @Min(1) @Max(5) Integer rating,
            @NotBlank @Size(max = 120) String comment) {
    }
}
