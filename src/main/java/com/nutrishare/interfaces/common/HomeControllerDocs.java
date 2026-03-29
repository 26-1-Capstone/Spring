package com.nutrishare.interfaces.common;

import com.nutrishare.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Home", description = "헬스 체크 API")
public interface HomeControllerDocs {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 동작 중인지 확인합니다.")
    ApiResponse<String> home();
}
