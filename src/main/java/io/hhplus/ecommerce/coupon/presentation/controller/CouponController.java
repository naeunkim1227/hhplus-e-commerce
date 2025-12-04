package io.hhplus.ecommerce.coupon.presentation.controller;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.coupon.domain.service.CouponIssueQueueService;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.coupon.presentation.dto.response.CouponIssueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Validated
public class CouponController {

    private final CouponIssueQueueService couponIssueQueueService;
    private final CouponService couponService;

    @PostMapping("/{couponId}/issue")
    @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰 발급 (동시성 제어 적용)")
    public CommonResponse<CouponIssueResponse> issueCoupon(
            @Parameter(description = "쿠폰 ID", example = "1")
            @PathVariable Long couponId,
            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId) {

        return CommonResponse.success(CouponIssueResponse.fromResult(couponIssueQueueService.addToQueue(userId, couponId)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "내 쿠폰 조회", description = "사용자가 발급받은 쿠폰 목록 조회")
    public CommonResponse<List<UserCoupon>> getUserCoupons(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId) {
        return CommonResponse.success(couponService.getUserCoupon(userId));
    }
}