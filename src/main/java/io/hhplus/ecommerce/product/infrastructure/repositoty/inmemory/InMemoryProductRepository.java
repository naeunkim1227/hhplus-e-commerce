package io.hhplus.ecommerce.product.infrastructure.repositoty.inmemory;

import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("test")
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public InMemoryProductRepository() {
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            Product newProduct = Product.builder()
                    .id(newId)
                    .name(product.getName())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .status(product.getStatus())
                    .version(0L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            store.put(newId, newProduct);
            return newProduct;
        } else {
            // 업데이트
            store.put(product.getId(), product);
            return product;
        }
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return List.of();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

}