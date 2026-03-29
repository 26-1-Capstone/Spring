package com.nutrishare.groupbuying.interfaces;

import com.nutrishare.common.api.ApiResponse;
import com.nutrishare.groupbuying.interfaces.GroupBuyingController.CreateGroupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "GroupBuying", description = "공동구매 관련 API")
public interface GroupBuyingControllerDocs {

    @Operation(summary = "공동구매 생성", description = "새로운 공동구매를 생성합니다.")
    ApiResponse<Map<String, Object>> createGroup(Authentication authentication, @RequestBody @Valid CreateGroupRequest request);

    @Operation(summary = "공동구매 취소", description = "생성된 공동구매를 취소합니다.")
    ApiResponse<Map<String, Object>> cancelGroup(Authentication authentication,
            @Parameter(description = "공동구매 ID") @PathVariable Long id);
}
