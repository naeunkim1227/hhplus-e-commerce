package io.hhplus.ecommerce.fixture;

import io.hhplus.ecommerce.user.domain.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User 엔티티 테스트 픽스처
 * 테스트에서 자주 사용되는 User 객체를 생성하는 유틸리티 클래스
 */
public class UserFixture {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 사용자 생성 (잔액 충분)
     */
    public static User defaultUser() {
        return User.builder()
                .name("김뿌꾸")
                .balance(BigDecimal.valueOf(1000000))
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }


    /**
     * 잔액이 적은 사용자
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static User lowBalanceUser() {
        return User.builder()
                .name("이두치")
                .balance(BigDecimal.valueOf(1000))
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 잔액이 없는 사용자 생성
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static User noBalanceUser() {
        return User.builder()
                .name("하뿌꾸")
                .balance(BigDecimal.ZERO)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

}