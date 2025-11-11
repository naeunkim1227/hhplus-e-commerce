package io.hhplus.ecommerce.coupon.infrastructure.repositoty.inmemory;

import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("test")
public class InMemoryCouponRepository implements CouponRepository {

    private final Map<Long, Coupon> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public InMemoryCouponRepository() {
    }

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            Coupon newCoupon = Coupon.builder()
                    .id(newId)
                    .code(coupon.getCode())
                    .name(coupon.getName())
                    .totalQuantity(coupon.getTotalQuantity())
                    .issuedQuantity(coupon.getIssuedQuantity())
                    .startDate(coupon.getStartDate())
                    .endDate(coupon.getEndDate())
                    .status(coupon.getStatus())
                    .version(0L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            store.put(newId, newCoupon);
            return newCoupon;
        } else {
            // 업데이트
            store.put(coupon.getId(), coupon);
            return coupon;
        }
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return store.values().stream()
                .filter(coupon -> coupon.getCode().equals(code))
                .findFirst();
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(store.values());
    }

}