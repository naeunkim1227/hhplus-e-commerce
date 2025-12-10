package io.hhplus.ecommerce.cart.domain.listener;

import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@Slf4j
public class CartEventListener {
    private final CartService cartService;

    @Async
    @TransactionalEventListener
    public void handleOrderCompleted(OrderCompletedEvent event){
        if (event.getCartItemIds() != null && !event.getCartItemIds().isEmpty()) {
            cartService.clearCartItems(event.getCartItemIds());
            log.info("장바구니 비우기 완료 - orderId: {}, cartItemIds: {}", event.getOrderId(), event.getCartItemIds());
        }
    }

}
