package io.hhplus.ecommerce.order.infrastructure;

import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryOrderItemRepository implements OrderItemRepository {

    private final Map<Long, OrderItem> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public InMemoryOrderItemRepository() {
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            OrderItem newOrderItem = OrderItem.builder()
                    .id(newId)
                    .orderId(orderItem.getOrderId())
                    .productId(orderItem.getProductId())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
            store.put(newId, newOrderItem);
            return newOrderItem;
        } else {
            // 업데이트
            store.put(orderItem.getId(), orderItem);
            return orderItem;
        }
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(orderItem -> orderItem.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItem> findAll() {
        return new ArrayList<>(store.values());
    }
}