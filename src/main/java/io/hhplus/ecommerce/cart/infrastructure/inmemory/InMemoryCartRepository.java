package io.hhplus.ecommerce.cart.infrastructure.inmemory;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@Profile("test")
public class InMemoryCartRepository implements CartRepository {

    private final Map<Long, CartItem> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public InMemoryCartRepository() {
    }

    @Override
    public CartItem save(CartItem cartItem) {
        if (cartItem.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            CartItem newCartItem = CartItem.builder()
                    .id(newId)
                    .userId(cartItem.getUserId())
                    .productId(cartItem.getProductId())
                    .quantity(cartItem.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            store.put(newId, newCartItem);
            return newCartItem;
        } else {
            // 업데이트
            store.put(cartItem.getId(), cartItem);
            return cartItem;
        }
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return store.values().stream()
                .filter(cartItem -> cartItem.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
        return store.values().stream()
                .filter(cartItem -> cartItem.getUserId().equals(userId)
                        && cartItem.getProductId().equals(productId))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        List<Long> idsToRemove = store.values().stream()
                .filter(cartItem -> cartItem.getUserId().equals(userId))
                .map(CartItem::getId)
                .collect(Collectors.toList());
        idsToRemove.forEach(store::remove);
    }

}