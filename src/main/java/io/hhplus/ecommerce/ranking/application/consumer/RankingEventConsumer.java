package io.hhplus.ecommerce.ranking.application.consumer;

import io.hhplus.ecommerce.common.kafka.KafkaTopics;
import io.hhplus.ecommerce.common.kafka.consumer.EventProcessTemplate;
import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingEventConsumer {

    private final EventProcessTemplate eventTemplate;
    private final RankingService rankingService;

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "ranking-group")
    public void onOrderCompleted(@Payload String message, Acknowledgment ack) {
        log.error("하이!!!!!!!!!!!");
        eventTemplate.execute(message, OrderCompletedEvent.class, event -> {
            rankingService.recordOrder(event.getOrderId());
        }, ack);
    }

}
