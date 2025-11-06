package io.hhplus.ecommerce.queue;



public interface QueueManager {
    void submit(String key, Runnable runnable);
    void startProcess();
}
