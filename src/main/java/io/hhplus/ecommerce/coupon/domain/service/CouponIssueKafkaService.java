package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.coupon.application.result.CouponIssueDto;
import io.hhplus.ecommerce.coupon.infrastructure.kafka.CouponIssueKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueKafkaService {

    private final CouponIssueKafkaProducer kafkaProducer;

    public CouponIssueDto requestCouponIssue(Long couponId, Long userId) {
        log.info("Requesting coupon issue - couponId: {}, userId: {}", couponId, userId);

        String requestId = kafkaProducer.publishIssueRequest(couponId, userId);

        log.info("Coupon issue requested - requestId: {}, couponId: {}, userId: {}",
            requestId, couponId, userId);

        // requestId는 UUID 포함 문자열이므로 waitNumber는 0으로 설정
        return CouponIssueDto.from(couponId, userId, 0L);
    }


    // public CouponIssueStatus getIssueStatus(String requestId) { ... }
}