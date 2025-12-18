package io.hhplus.ecommerce.common.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
    List<Outbox> findAllByStatus(OutboxStatus status);
}