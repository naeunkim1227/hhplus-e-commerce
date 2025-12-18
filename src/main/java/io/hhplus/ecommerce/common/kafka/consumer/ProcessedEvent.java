package io.hhplus.ecommerce.common.kafka.consumer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    private String eventId;

    private String eventType;

    private String domain;

    /**
     * 처리 완료 시각
     */
    @CreationTimestamp
    private LocalDateTime processedAt;

    public ProcessedEvent(String eventId, String eventType, String domain) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.domain = domain;
    }
}