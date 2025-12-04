package io.hhplus.ecommerce.common.stream;


import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueStreamConsumer implements StreamListener<String, MapRecord<String, String, String>>
{

    private final CouponService couponService;
    private final RedisTemplate redisTemplate;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String streamKey = message.getStream();
        String messageId = message.getId().getValue();
        Map<String, String> body = message.getValue();

        log.info("Consumer 메시지 수신");


        try {
            String couponIdStr = String.valueOf(body.get("couponId"));
            String userIdStr = String.valueOf(body.get("userId"));
            String timestamp = String.valueOf(body.get("timestamp"));

            Long couponId = Long.valueOf(couponIdStr);
            Long userId = Long.valueOf(userIdStr);

            log.info("쿠폰 발급 메세지 수신 성공  - Stream: {}, MessageId: {}, UserId: {}, CouponId: {}, Timestamp: {}",
                    streamKey, messageId, userId, couponId, timestamp);

            // 실제 쿠폰 발급 처리 (DB 저장)
            couponService.issueCoupon(userId, couponId);
            log.info("쿠폰 발급 성공 - UserId: {}, CouponId: {}, MessageId: {}",
                    userId, couponId, messageId);

        } catch (BusinessException e) {

        } catch (Exception e) {
            throw new RuntimeException("Failed to process coupon issue message", e);
        }
    }
}
