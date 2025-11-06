package io.hhplus.ecommerce.user.infrastructure;

import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.infrastructure.repositoty.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public InMemoryUserRepository() {
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            User newUser = User.builder()
                    .id(newId)
                    .name(user.getName())
                    .balance(user.getBalance())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            store.put(newId, newUser);
            return newUser;
        } else {
            // 업데이트
            store.put(user.getId(), user);
            return user;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }


    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

}