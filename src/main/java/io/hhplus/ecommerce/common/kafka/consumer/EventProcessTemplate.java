package io.hhplus.ecommerce.common.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.ecommerce.common.kafka.consumer.ProcessedEvent;
import io.hhplus.ecommerce.common.kafka.consumer.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

/**
 * Kafka 이벤트 처리 템플릿
 * - 래핑된 메시지 파싱 {id, type, domain, payload}
 * - 중복 이벤트 체크
 * - JSON → 이벤트 객체 역직렬화
 * - 비즈니스 로직 실행
 * - 처리 완료 기록 및 ACK
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventProcessTemplate {

    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;

    /**
     * 이벤트 처리 실행
     *
     * @param message    Kafka 메시지 (래핑된 JSON: {id, type, domain, payload})
     * @param eventClass 이벤트 클래스 타입
     * @param handler    비즈니스 로직 핸들러
     * @param ack        Kafka Acknowledgment
     * @param <T>        이벤트 타입
     */
    @Transactional
    public <T> void execute(
            String message,
            Class<T> eventClass,
            Consumer<T> handler,
            Acknowledgment ack
    ) {
        try {
            // 1. 래핑된 메시지 파싱
            JsonNode messageNode = objectMapper.readTree(message);
            String eventId = messageNode.get("id").asText();
            String eventType = messageNode.get("type").asText();
            String domain = messageNode.get("domain").asText();
            String payload = messageNode.get("payload").toString();

            log.debug("이벤트 수신 - eventId={}, type={}, domain={}", eventId, eventType, domain);

            // 2. 중복 이벤트 체크
            if (processedEventRepository.existsById(eventId)) {
                log.info("이미 처리된 이벤트입니다. eventId={}", eventId);
                ack.acknowledge();
                return;
            }

            // 3. payload를 실제 이벤트 객체로 역직렬화
            T event = objectMapper.readValue(payload, eventClass);

            handler.accept(event);

            // 5. 처리 완료 기록
            ProcessedEvent processedEvent = new ProcessedEvent(
                    eventId,
                    eventType,
                    domain
            );
            processedEventRepository.save(processedEvent);

            // 6. Kafka 커밋
            ack.acknowledge();

            log.info("이벤트 처리 완료. eventId={}, eventType={}, domain={}", eventId, eventType, domain);

        } catch (Exception e) {
            log.error("이벤트 처리 실패. message={}, error={}", message, e.getMessage(), e);
            throw new RuntimeException("이벤트 처리 중 오류 발생", e);
        }
    }
}