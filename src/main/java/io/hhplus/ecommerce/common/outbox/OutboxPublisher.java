package io.hhplus.ecommerce.common.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;

    void publish(Outbox outbox) {
        try {
            String wrappedMessage = String.format("""
                {
                    "id": "%s",
                    "type": "%s",
                    "domain": "%s",
                    "payload": %s
                }
                """, outbox.getId(), outbox.getType(), outbox.getDomain(), outbox.getPayload());

            kafkaTemplate.send(outbox.getTopic(), outbox.getEventKey(), wrappedMessage)
                    .whenComplete((recordMetadata, e) -> {
                        if(e == null){
                            outbox.markAsPublished();
                            log.info("Successfully published to Kafka - outboxId: {}, topic: {}",
                                    outbox.getId(), outbox.getTopic());
                        }else{
                            outbox.markAsFailed();
                            log.error("Failed to publish to Kafka - outboxId: {}", outbox.getId(), e);
                        }
                    });

            outboxRepository.save(outbox);

        } catch (Exception e) {
            log.error("Failed to send message to Kafka - outboxId: {}", outbox.getId(), e);
            throw e;
        }
    }

}
