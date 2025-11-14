package io.hhplus.ecommerce.order.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query("SELECT COALESCE(MAX(o.id), 0) + 1 FROM Order o")
    Long getNextId();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.userId = :userId")
    List<Order> findByUserIdWithOrderItems(@Param("userId") Long userId);
}