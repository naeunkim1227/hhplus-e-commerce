package io.hhplus.ecommerce.ranking.domain.listener;
import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;

import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

}
