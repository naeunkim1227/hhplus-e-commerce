package io.hhplus.ecommerce.user.presentation.dto.request;

import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Positive
    private BigDecimal balance;

    public UserCreateCommand toCommand() {
        return UserCreateCommand.builder()
                .id(id)
                .name(name)
                .balance(balance)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
