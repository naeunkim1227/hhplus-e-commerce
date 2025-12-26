package io.hhplus.ecommerce.coupon.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.ecommerce.coupon.application.dto.command.CouponIssueCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "coupon-issue-requests";

    public String publishIssueRequest(Long couponId, Long userId) {
        try {
            // 중복 요청 방지를 위한 고유 ID
            String requestId = generateRequestId(couponId, userId);

            CouponIssueCommand command = new CouponIssueCommand(
                couponId, userId, requestId
            );

            // EventProcessTemplate이 기대하는 형식으로 메시지 래핑
            String payload = objectMapper.writeValueAsString(command);
            String wrappedMessage = String.format("""
                {
                    "id": "%s",
                    "type": "CouponIssueCommand",
                    "domain": "COUPON",
                    "payload": %s
                }
                """, requestId, payload);

            String key = couponId.toString();

            kafkaTemplate.send(TOPIC, key, wrappedMessage)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish coupon issue request - couponId: {}, userId: {}",
                            couponId, userId, ex);
                    } else {
                        log.info("Published coupon issue request - couponId: {}, userId: {}, requestId: {}, partition: {}",
                            couponId, userId, requestId, result.getRecordMetadata().partition());
                    }
                });

            return requestId;

        } catch (Exception e) {
            log.error("Error publishing coupon issue request", e);
            throw new RuntimeException("Failed to publish coupon issue request", e);
        }
    }

    private String generateRequestId(Long couponId, Long userId) {
        return String.format("%d-%d-%s", couponId, userId, UUID.randomUUID());
    }
}