package io.hhplus.ecommerce.user.domain.entity;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import io.hhplus.ecommerce.user.domain.exception.UserErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static User create(UserCreateCommand command) {
        return User.builder()
                .name(command.getName())
                .balance(command.getBalance())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void addBalance(BigDecimal amount) {
        this.balance.add(amount);
    }

    public void reduceBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException(UserErrorCode.INSUFFICIENT_BALANCE);
        }

        this.balance.subtract(amount);
    }
}