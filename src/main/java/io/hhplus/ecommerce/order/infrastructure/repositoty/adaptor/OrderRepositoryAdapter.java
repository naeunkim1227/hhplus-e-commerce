package io.hhplus.ecommerce.order.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import io.hhplus.ecommerce.order.infrastructure.repositoty.jpa.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaRepository;

    @Override
    public Long nextId() {
        return jpaRepository.getNextId();
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Order> findByIdWithOrderItems(Long orderId) {
        return jpaRepository.findByIdWithOrderItems(orderId);
    }

    @Override
    public List<Order> findByUserIdWithOrderItems(Long userId) {
        return jpaRepository.findByUserIdWithOrderItems(userId);
    }
}