package io.hhplus.ecommerce.order.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query("SELECT COALESCE(MAX(o.id), 0) + 1 FROM Order o")
    Long getNextId();
}