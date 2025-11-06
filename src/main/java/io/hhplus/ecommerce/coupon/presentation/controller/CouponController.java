package io.hhplus.ecommerce.coupon.presentation.controller;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.coupon.application.usecase.CouponUserUseCase;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Validated
public class CouponController {

    private final CouponUserUseCase couponUserUseCase;

    @PostMapping("/{couponId}/issue")
    public CommonResponse<Map<String, Object>> issueCoupon(
            @Parameter(description = "쿠폰 ID", example = "1")
            @PathVariable Long couponId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "선착순 쿠폰 발급 API - Mock");

        return CommonResponse.success(HttpStatus.CREATED.value(), data);
    }

    @GetMapping("/users/coupons")
    public CommonResponse<List> getUserCoupons(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId) {

        List<UserCoupon> userCouponList = couponUserUseCase.excute(userId);
        return CommonResponse.success(userCouponList);
    }
}