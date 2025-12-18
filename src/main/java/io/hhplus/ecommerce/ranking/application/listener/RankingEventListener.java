package io.hhplus.ecommerce.ranking.application.listener;

import io.hhplus.ecommerce.cart.domain.event.CartAddedEvent;
import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;
import io.hhplus.ecommerce.product.domain.event.ProductLikedEvent;
import io.hhplus.ecommerce.product.domain.event.ProductUnLikedEvent;
import io.hhplus.ecommerce.product.domain.event.ProductViewedEvent;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@AllArgsConstructor
@Slf4j
public class RankingEventListener {

    private final RankingService rankingService;

    @Async
    @TransactionalEventListener
    public void handleOrderCompleted(OrderCompletedEvent event){
        rankingService.recordOrder(event.getOrderId());
    }

    @Async
    @EventListener
    public void handleProductViewed(ProductViewedEvent event) {
        rankingService.recordView(event.getProductId(), event.getUserId());
    }

    @Async
    @EventListener
    public void handleProductLiked(ProductLikedEvent event) {
        rankingService.recordLike(event.getProductId());
    }

    @Async
    @EventListener
    public void handleProductUnliked(ProductUnLikedEvent event) {
        rankingService.removeLike(event.getProductId());
    }

    @TransactionalEventListener
    public void handleCartAdded(CartAddedEvent event) {
        rankingService.recordCart(event.getProductId(), event.getUserId());
    }


}
