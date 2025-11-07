package io.hhplus.ecommerce.coupon.presentation.controller;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.coupon.application.usecase.CouponIssueUseCase;
import io.hhplus.ecommerce.coupon.application.usecase.CouponUserUseCase;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.queue.QueueAnnotation;
import io.swagger.v3.oas.annotations.Operation;
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
    private final CouponIssueUseCase couponIssueUseCase;

    @PostMapping("/{couponId}/issue")
    @QueueAnnotation(topic = "coupon", key = "#userId")  // 동시성 제어 적용
    @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰 발급 (동시성 제어 적용)")
    public CommonResponse<UserCoupon> issueCoupon(
            @Parameter(description = "쿠폰 ID", example = "1")
            @PathVariable Long couponId,
            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId) {

        UserCoupon userCoupon = couponIssueUseCase.execute(userId, couponId);
        return CommonResponse.success(userCoupon);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "내 쿠폰 조회", description = "사용자가 발급받은 쿠폰 목록 조회")
    public CommonResponse<List<UserCoupon>> getUserCoupons(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId) {
        return CommonResponse.success(couponUserUseCase.excute(userId));
    }
}