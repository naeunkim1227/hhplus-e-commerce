package io.hhplus.ecommerce.order.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;
import io.hhplus.ecommerce.order.infrastructure.repositoty.jpa.JpaOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderItemRepositoryAdapter implements OrderItemRepository {

    private final JpaOrderItemRepository jpaRepository;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return jpaRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<OrderItem> findAll() {
        return jpaRepository.findAll();
    }
}