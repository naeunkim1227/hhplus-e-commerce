package io.hhplus.ecommerce.coupon.infrastructure;

import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryUserCouponRepository implements UserCouponRepository {

    private final Map<Long, UserCoupon> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        if (userCoupon.getId() == null) {
            // 신규 저장
            Long newId = idGenerator.getAndIncrement();
            UserCoupon newUserCoupon = UserCoupon.builder()
                    .id(newId)
                    .userId(userCoupon.getUserId())
                    .couponId(userCoupon.getCouponId())
                    .issuedAt(userCoupon.getIssuedAt())
                    .usedAt(userCoupon.getUsedAt())
                    .expiredAt(userCoupon.getExpiredAt())
                    .build();
            store.put(newId, newUserCoupon);
            return newUserCoupon;
        } else {
            // 업데이트
            store.put(userCoupon.getId(), userCoupon);
            return userCoupon;
        }
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return store.values().stream()
                .filter(uc -> uc.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {
        return store.values().stream()
                .filter(uc -> uc.getUserId().equals(userId) && uc.getCouponId().equals(couponId))
                .findFirst();
    }

    @Override
    public List<UserCoupon> findByCouponId(Long couponId) {
        return store.values().stream()
                .filter(uc -> uc.getCouponId().equals(couponId))
                .collect(Collectors.toList());
    }

}