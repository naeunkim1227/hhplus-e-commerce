package io.hhplus.ecommerce.common.outbox;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final OutboxPublisher publisher;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        List<Outbox> pendingEvents = outboxRepository.findAllByStatus(OutboxStatus.PENDING);
        pendingEvents.forEach(publisher::publish);
    }

}
