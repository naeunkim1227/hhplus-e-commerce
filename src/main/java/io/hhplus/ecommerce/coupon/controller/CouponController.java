package io.hhplus.ecommerce.coupon.controller;

import io.hhplus.ecommerce.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    @GetMapping
    public ApiResponse<Map<String, Object>> getCoupons() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "발급 가능한 쿠폰 목록 API - Mock");
        return ApiResponse.success(data);
    }

    @PostMapping("/{couponId}/issue")
    public ApiResponse<Map<String, Object>> issueCoupon(
            @Parameter(description = "쿠폰 ID", example = "1")
            @PathVariable Long couponId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "선착순 쿠폰 발급 API - Mock");
        return ApiResponse.success(HttpStatus.CREATED.value(), data);
    }

    @GetMapping("/users/coupons")
    public ApiResponse<Map<String, Object>> getUserCoupons(
            @Parameter(description = "쿠폰 상태 (AVAILABLE, USED, EXPIRED)")
            @RequestParam(required = false) String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "내 쿠폰 조회 API - Mock");
        return ApiResponse.success(data);
    }

    @GetMapping("/validate")
    public ApiResponse<Map<String, Object>> validateCoupon(
            @Parameter(description = "쿠폰 ID", example = "1")
            @RequestParam Long couponId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "쿠폰 적용 가능 여부 조회 API - Mock");
        return ApiResponse.success(data);
    }
}