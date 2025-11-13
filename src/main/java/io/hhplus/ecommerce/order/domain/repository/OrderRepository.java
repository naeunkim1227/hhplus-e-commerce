package io.hhplus.ecommerce.order.domain.repository;

import io.hhplus.ecommerce.order.domain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Long nextId();
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findAll();

    /**
     * Order와 OrderItem을 함께 조회
     */
    Optional<Order> findByIdWithOrderItems(Long orderId);

    /**
     * 사용자의 모든 Order와 OrderItem을 함께 조회
     */
    List<Order> findByUserIdWithOrderItems(Long userId);
}