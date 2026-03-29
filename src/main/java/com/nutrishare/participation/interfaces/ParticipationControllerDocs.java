package com.nutrishare.participation.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.participation.interfaces.ParticipationController.JoinRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Participation", description = "공동구매 참여 API")
public interface ParticipationControllerDocs {

    @Operation(summary = "공동구매 참여", description = "공동구매에 참여합니다.")
    ApiResponse<Map<String, Object>> joinGroup(
            Authentication authentication,
            @Parameter(description = "공동구매 ID") @PathVariable Long groupId,
            @RequestBody @Valid JoinRequest request);
}
