package com.nutrishare.catalog.interfaces;

import com.nutrishare.catalog.application.CatalogCommandService;
import com.nutrishare.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerDocs {

    private final CatalogCommandService catalogCommandService;

    @PostMapping
    public ApiResponse<Map<String, Object>> createCategory(@RequestBody @Valid CreateCategoryRequest request) {
        Long categoryId = catalogCommandService.createCategory(request.name());

        return ApiResponse.success(Map.of(
                "resourceId", categoryId,
                "status", "CREATED"));
    }

    @io.swagger.v3.oas.annotations.media.Schema(description = "카테고리 생성 요청")
    public record CreateCategoryRequest(
            @io.swagger.v3.oas.annotations.media.Schema(description = "카테고리명", example = "과일") @NotBlank String name) {
    }
}
