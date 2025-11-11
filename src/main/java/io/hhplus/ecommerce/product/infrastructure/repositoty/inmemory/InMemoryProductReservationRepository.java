package io.hhplus.ecommerce.product.infrastructure.repositoty.inmemory;

import io.hhplus.ecommerce.product.domain.entity.ProductReservation;
import io.hhplus.ecommerce.product.domain.entity.ProductReservationStatus;
import io.hhplus.ecommerce.product.domain.repository.ProductReservationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 상품 재고 선점 InMemory Repository
 */
@Repository
@Profile("test")
public class InMemoryProductReservationRepository implements ProductReservationRepository {

    private final Map<Long, ProductReservation> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public ProductReservation save(ProductReservation reservation) {
        if (reservation.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            ProductReservation newReservation = ProductReservation.builder()
                    .id(newId)
                    .orderId(reservation.getOrderId())
                    .productId(reservation.getProductId())
                    .quantity(reservation.getQuantity())
                    .status(reservation.getStatus())
                    .expiredAt(reservation.getExpiredAt())
                    .releasedAt(reservation.getReleasedAt())
                    .createdAt(reservation.getCreatedAt())
                    .updatedAt(reservation.getUpdatedAt())
                    .build();
            store.put(newId, newReservation);
            return newReservation;
        } else {
            // 업데이트
            store.put(reservation.getId(), reservation);
            return reservation;
        }
    }

    @Override
    public Optional<ProductReservation> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<ProductReservation> findByOrderId(Long userId) {
        return store.values().stream()
                .filter(r -> r.getOrderId().equals(userId))
                .findFirst();
    }

    @Override
    public List<ProductReservation> findActiveReservationsByProductId(Long productId) {
        return store.values().stream()
                .filter(r -> r.getProductId().equals(productId))
                .filter(ProductReservation::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public Long getTotalReservedQuantity(Long productId) {
        return store.values().stream()
                .filter(r -> r.getProductId().equals(productId))
                .filter(ProductReservation::isActive)
                .mapToLong(ProductReservation::getQuantity)
                .sum();
    }

    @Override
    public List<ProductReservation> findExpiredReservations(LocalDateTime now) {
        return store.values().stream()
                .filter(r -> r.getStatus() == ProductReservationStatus.ACTIVE)
                .filter(r -> r.getExpiredAt().isBefore(now))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}