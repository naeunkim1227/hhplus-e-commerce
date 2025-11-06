package io.hhplus.ecommerce.order.infrastructure;

import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public InMemoryOrderRepository() {
    }

    @Override
    public Long nextId() {
        return idGenerator.getAndIncrement();
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            Order newOrder = Order.builder()
                    .id(newId)
                    .userId(order.getUserId())
                    .status(order.getStatus())
                    .couponId(order.getCouponId())
                    .totalAmount(order.getTotalAmount())
                    .discountAmount(order.getDiscountAmount())
                    .finalAmount(order.getFinalAmount())
                    .createdAt(LocalDateTime.now())
                    .orderedAt(order.getOrderedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            store.put(newId, newOrder);
            return newOrder;
        } else {
            // 업데이트
            store.put(order.getId(), order);
            return order;
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return store.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

}