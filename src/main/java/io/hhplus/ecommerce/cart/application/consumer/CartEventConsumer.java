package io.hhplus.ecommerce.cart.application.consumer;


import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.common.kafka.KafkaTopics;
import io.hhplus.ecommerce.common.kafka.consumer.EventProcessTemplate;
import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CartEventConsumer {

    private final CartService cartService;
    private final EventProcessTemplate eventTemplate;

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "cart-group")
    public void onOrderCompleted(@Payload String message, Acknowledgment ack) {
        eventTemplate.execute(message, OrderCompletedEvent.class, event -> {
            if(event.getCartItemIds() == null || event.getCartItemIds().isEmpty()) return;
            cartService.clearCartItems(event.getCartItemIds());
        }, ack);
    }

}
