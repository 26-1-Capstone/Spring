package com.nutrishare.catalog.interfaces;

import com.nutrishare.catalog.interfaces.CategoryController.CreateCategoryRequest;
import com.nutrishare.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Category", description = "카테고리 관련 API")
public interface CategoryControllerDocs {

    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    ApiResponse<Map<String, Object>> createCategory(@RequestBody @Valid CreateCategoryRequest request);
}
