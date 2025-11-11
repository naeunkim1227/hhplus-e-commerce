package io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaCouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);
}