package io.hhplus.ecommerce.queue.impl;

import io.hhplus.ecommerce.queue.QueueManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class CouponQueueManager implements QueueManager {

    private final ConcurrentHashMap<String, BlockingQueue<Runnable>> queue = new ConcurrentHashMap<>();

    @Override
    public void submit(String userId, Runnable runnable) {
        queue.computeIfAbsent(userId, k -> new LinkedBlockingDeque<>(1)).add(runnable);
    }

    @Override
    public void startProcess() {
        queue.forEach((userId, queue) -> {
            new Thread(() -> {
               while (true) {
                   try {
                       queue.take().run();
                   } catch (InterruptedException e) {
                       Thread.currentThread().interrupt();
                       break;
                   }
               }
            }).start();
        });
    }
}
