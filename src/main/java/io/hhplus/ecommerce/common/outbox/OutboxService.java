package io.hhplus.ecommerce.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void save(DomainType domain, String topic, String key, Object payload){
        try {
            log.info("Saving outbox event - domain: {}, topic: {}, key: {}, payload: {}", domain, topic, key, payload);

            String payloadJson = objectMapper.writeValueAsString(payload);
            String eventType = payload.getClass().getSimpleName();

            // 1. Outbox에 저장
            Outbox outbox = new Outbox(
                    topic,
                    domain,
                    eventType,
                    key,
                    payloadJson
            );

            outboxRepository.save(outbox);

            log.info("Successfully published event to Kafka - eventId: {}, topic: {}", outbox.getId(), topic);
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
            throw new RuntimeException("Failed to publish outbox event", e);
        }
    }




}
