package io.hhplus.ecommerce.user.presentation.dto.response;

import io.hhplus.ecommerce.user.application.dto.result.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public static UserResponse from(UserDto result) {
        return UserResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .balance(result.getBalance())
                .createdAt(result.getCreatedAt())
                .build();
    }
}