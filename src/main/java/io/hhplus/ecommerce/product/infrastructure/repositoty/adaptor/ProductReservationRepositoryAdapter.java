package io.hhplus.ecommerce.product.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.product.domain.entity.ProductReservation;
import io.hhplus.ecommerce.product.domain.repository.ProductReservationRepository;
import io.hhplus.ecommerce.product.infrastructure.repositoty.jpa.JpaProductReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ProductReservationRepositoryAdapter implements ProductReservationRepository {

    private final JpaProductReservationRepository jpaRepository;

    @Override
    public ProductReservation save(ProductReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public Optional<ProductReservation> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductReservation> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<ProductReservation> findActiveReservationsByProductId(Long productId) {
        return jpaRepository.findActiveReservationsByProductId(productId);
    }

    @Override
    public Long getTotalReservedQuantity(Long productId) {
        return jpaRepository.getTotalReservedQuantity(productId);
    }

    @Override
    public List<ProductReservation> findExpiredReservations(LocalDateTime now) {
        return jpaRepository.findExpiredReservations(now);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}