package io.hhplus.ecommerce.coupon.domain.repository;

import io.hhplus.ecommerce.coupon.domain.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(Long id);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findAll();
}