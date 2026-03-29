package com.nutrishare.interfaces.common;

import com.nutrishare.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController implements HomeControllerDocs {

    @GetMapping("/")
    public ApiResponse<String> home() {
        return ApiResponse.success("NutriShare API Server is running!");
    }
}
