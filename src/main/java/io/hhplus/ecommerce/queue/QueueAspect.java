package io.hhplus.ecommerce.queue;

import io.hhplus.ecommerce.queue.impl.CouponQueueManager;
import io.hhplus.ecommerce.queue.impl.OrderQueueManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class QueueAspect {

    private final CouponQueueManager couponQueueManager;
    private final OrderQueueManager orderQueueManager;

    @Around("@annotation(queueAnnotation)")
    public void around(ProceedingJoinPoint joinPoint, QueueAnnotation queueAnnotation) throws Throwable {
        Runnable task = () -> {
            try {
                joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
        switch (queueAnnotation.topic()) {
            case "coupon" -> couponQueueManager.submit(queueAnnotation.key(), task);
            case "order" ->  orderQueueManager.submit(queueAnnotation.key(), task);
        }
    }
}
