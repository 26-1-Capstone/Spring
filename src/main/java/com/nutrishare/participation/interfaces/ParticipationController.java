package com.nutrishare.participation.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.infrastructure.security.AuthUserExtractor;
import com.nutrishare.participation.application.ParticipationCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/join")
@RequiredArgsConstructor
public class ParticipationController implements ParticipationControllerDocs {

        private final ParticipationCommandService participationCommandService;

        @PostMapping
        public ApiResponse<Map<String, Object>> joinGroup(
                        Authentication authentication,
                        @PathVariable Long groupId,
                        @RequestBody @Valid JoinRequest request) {
                Long userId = AuthUserExtractor.getUserId(authentication);

                Long participationId = participationCommandService.joinGroup(userId, groupId, request.quantity());

                return ApiResponse.success(Map.of(
                                "resourceId", participationId,
                                "status", "ACCEPTED"));
        }

        // Cancel endpoint might be under /api/v1/participations/{id} based on REST or
        // /groups/{id}/leave
        // Spec says: POST /api/v1/groups/{id}/join
        // Cancellation isn't strictly defined in 4.5 section of Spec, but general rules
        // apply.
        // I'll add a cancel endpoint here or maybe a separate
        // ParticipationResourceController?
        // Let's stick to simple "Create" here.

        @io.swagger.v3.oas.annotations.media.Schema(description = "참여 요청")
        public record JoinRequest(
                        @io.swagger.v3.oas.annotations.media.Schema(description = "수량", example = "1") @NotNull @Min(1) Integer quantity) {
        }
}
