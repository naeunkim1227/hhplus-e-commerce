package io.hhplus.ecommerce.user.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Builder
@AllArgsConstructor
public class UserCreateCommand {
    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
