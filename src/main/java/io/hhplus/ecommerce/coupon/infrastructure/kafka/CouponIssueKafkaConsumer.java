package io.hhplus.ecommerce.coupon.infrastructure.kafka;

import io.hhplus.ecommerce.common.kafka.consumer.EventProcessTemplate;
import io.hhplus.ecommerce.coupon.application.dto.command.CouponIssueCommand;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueKafkaConsumer {

    private final EventProcessTemplate eventTemplate;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;


    @KafkaListener(
        topics = "coupon-issue-requests",
        groupId = "coupon-issue-processor",
        concurrency = "1"
    )
    public void consume(@Payload String message, Acknowledgment ack) {
        eventTemplate.execute(message, CouponIssueCommand.class, this::handleCouponIssue, ack);
    }


    @Transactional
    public void handleCouponIssue(CouponIssueCommand command) {
        log.info("[COUPON_ISSUE] Processing - requestId: {}, couponId: {}, userId: {}",
            command.getRequestId(), command.getCouponId(), command.getUserId());

        try {
            // 1. 중복 발급 체크
            boolean alreadyIssued = userCouponRepository.existsByCouponIdAndUserId(
                command.getCouponId(), command.getUserId()
            );

            if (alreadyIssued) {
                log.warn("[COUPON_ISSUE] Already issued - couponId: {}, userId: {}",
                    command.getCouponId(), command.getUserId());
                return;
            }

            // 2. 쿠폰 조회
            Coupon coupon = couponRepository.findById(command.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "쿠폰을 찾을 수 없습니다: " + command.getCouponId()
                ));

            // 3. 재고 확인
            if (coupon.getIssuedQuantity() >= coupon.getTotalQuantity()) {
                log.warn("[COUPON_ISSUE] Out of stock - couponId: {}, issued: {}, total: {}",
                    command.getCouponId(), coupon.getIssuedQuantity(), coupon.getTotalQuantity());
                return; // 재고 소진
            }

            // 4. 쿠폰 발급
            UserCoupon userCoupon = UserCoupon.create(command.getUserId(), command.getCouponId());
            userCouponRepository.save(userCoupon);

            // 5. 재고 차감
            coupon.increaseIssuedQuantity();
            couponRepository.save(coupon);

            log.info("[COUPON_ISSUE] Successfully issued - couponId: {}, userId: {}, remaining: {}/{}",
                command.getCouponId(), command.getUserId(),
                coupon.getTotalQuantity() - coupon.getIssuedQuantity(),
                coupon.getTotalQuantity());

        } catch (Exception e) {
            log.error("[COUPON_ISSUE] Failed - requestId: {}, couponId: {}, userId: {}",
                command.getRequestId(), command.getCouponId(), command.getUserId(), e);
            throw e; // 재시도를 위해 예외 전파
        }
    }
}