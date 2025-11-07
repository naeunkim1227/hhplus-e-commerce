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
}