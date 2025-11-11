package io.hhplus.ecommerce.coupon.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {

    private final JpaCouponRepository jpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<Coupon> findAll() {
        return jpaRepository.findAll();
    }
}