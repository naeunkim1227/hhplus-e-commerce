package io.hhplus.ecommerce.order.controller;

import io.hhplus.ecommerce.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    public ApiResponse<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> request) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "주문 생성 API - Mock");
        return ApiResponse.success(HttpStatus.CREATED.value(), data);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<Map<String, Object>> getOrder(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable Long orderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "주문 상세 조회 API - Mock");
        return ApiResponse.success(data);
    }
}