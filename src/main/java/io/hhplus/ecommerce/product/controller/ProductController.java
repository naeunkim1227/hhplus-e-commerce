package io.hhplus.ecommerce.product.controller;

import io.hhplus.ecommerce.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public ApiResponse<Map<String, Object>> getProducts(
            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "상품 목록 조회 API - Mock");
        return ApiResponse.success(data);
    }

    @GetMapping("/{productId}")
    public ApiResponse<Map<String, Object>> getProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "상품 상세 조회 API - Mock");
        return ApiResponse.success(data);
    }

    @GetMapping("/popular")
    public ApiResponse<Map<String, Object>> getPopularProducts() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "인기 상품 조회 API - Mock");
        return ApiResponse.success(data);
    }
}