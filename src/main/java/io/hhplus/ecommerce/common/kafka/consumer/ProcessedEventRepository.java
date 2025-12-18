package io.hhplus.ecommerce.common.kafka.consumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

}