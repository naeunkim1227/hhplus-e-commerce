package io.hhplus.ecommerce.user.application.dto.result;

import io.hhplus.ecommerce.user.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 조회 결과 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                .name(user.getName())
                .balance(user.getBalance())
                .build();
    }
}