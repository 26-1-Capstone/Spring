package com.nutrishare.groupbuying.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.groupbuying.application.GroupBuyingCommandService;
import com.nutrishare.infrastructure.security.AuthUserExtractor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import com.nutrishare.groupbuying.application.GroupBuyingQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupBuyingController implements GroupBuyingControllerDocs {

        private final GroupBuyingCommandService groupBuyingCommandService;
        private final GroupBuyingQueryService groupBuyingQueryService;

        @PostMapping
        public ApiResponse<Map<String, Object>> createGroup(
                        Authentication authentication,
                        @RequestBody @Valid CreateGroupRequest request) {
                Long userId = AuthUserExtractor.getUserId(authentication);

                Long groupId = groupBuyingCommandService.createGroupPurchase(
                                userId,
                                request.productId(),
                                request.title(),
                                request.description(),
                                request.targetQuantity(),
                                request.unitPrice(),
                                request.endAt());

                return ApiResponse.success(Map.of(
                                "resourceId", groupId,
                                "status", "CREATED"));
        }

        @GetMapping
        public ApiResponse<Page<GroupBuyingQueryService.GroupPurchaseSummaryView>> getGroups(Pageable pageable) {
                Page<GroupBuyingQueryService.GroupPurchaseSummaryView> page = groupBuyingQueryService
                                .getGroupPurchases(pageable);
                return ApiResponse.success(page);
        }

        @GetMapping("/{id}")
        public ApiResponse<GroupBuyingQueryService.GroupPurchaseDetailView> getGroupDetail(@PathVariable Long id) {
                GroupBuyingQueryService.GroupPurchaseDetailView detail = groupBuyingQueryService
                                .getGroupPurchaseDetail(id);
                return ApiResponse.success(detail);
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Map<String, Object>> cancelGroup(
                        Authentication authentication,
                        @PathVariable Long id) {
                Long userId = AuthUserExtractor.getUserId(authentication);

                groupBuyingCommandService.cancelGroupPurchase(userId, id);

                return ApiResponse.success(Map.of(
                                "resourceId", id,
                                "status", "CANCELED"));
        }

        @io.swagger.v3.oas.annotations.media.Schema(description = "공동구매 생성 요청")
        public record CreateGroupRequest(
                        @io.swagger.v3.oas.annotations.media.Schema(description = "상품 ID", example = "1") @NotNull Long productId,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "제목", example = "같이 사과 사요") @NotBlank String title,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "설명", example = "10명 모이면 할인됩니다.") String description,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "목표 수량", example = "10") @NotNull Integer targetQuantity,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "단가", example = "12000") @NotNull Long unitPrice,
                        @io.swagger.v3.oas.annotations.media.Schema(description = "종료 일시") @NotNull @Future LocalDateTime endAt) {
        }
}
