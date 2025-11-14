package io.hhplus.ecommerce.queue;

import io.hhplus.ecommerce.queue.impl.CouponQueueManager;
import io.hhplus.ecommerce.queue.impl.OrderQueueManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * 큐 매니저 초기화 설정
 * 애플리케이션 시작 시 큐 처리 스레드를 시작합니다.
 */
@Configuration
@RequiredArgsConstructor
public class QueueConfig {

    private final CouponQueueManager couponQueueManager;
    private final OrderQueueManager orderQueueManager;

    @PostConstruct
    public void init() {
        // 쿠폰 큐 프로세스 시작
        couponQueueManager.startProcess();

        // 주문 큐 프로세스 시작
        orderQueueManager.startProcess();
    }
}