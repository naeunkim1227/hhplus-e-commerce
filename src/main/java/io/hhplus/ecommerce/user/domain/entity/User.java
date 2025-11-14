package io.hhplus.ecommerce.user.domain.entity;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import io.hhplus.ecommerce.user.domain.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static User create(UserCreateCommand command) {
        return User.builder()
                .name(command.getName())
                .balance(command.getBalance())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void reduceBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException(UserErrorCode.INSUFFICIENT_BALANCE);
        }

        this.balance = this.balance.subtract(amount);
    }
}