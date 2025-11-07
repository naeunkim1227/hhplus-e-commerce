package io.hhplus.ecommerce.queue.impl;

import io.hhplus.ecommerce.queue.QueueManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderQueueManager implements QueueManager {

    private final ConcurrentHashMap<String, BlockingQueue<Runnable>> queue = new ConcurrentHashMap<>();

    @Override
    public void submit(String productId, Runnable runnable) {

    }

    @Override
    public void startProcess() {

    }
}
