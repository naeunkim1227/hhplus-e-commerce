package io.hhplus.ecommerce.user.domain;

import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.domain.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class UserServiceJpaTest {

    @Autowired
    private UserService userService;

    private UserCreateCommand createCommand(String name, BigDecimal balance) {
        return UserCreateCommand.builder()
                .name(name)
                .balance(balance)
                .build();
    }

    @Test
    void createAndFetchUser() {
        // 1. 유저 생성
        UserCreateCommand command = createCommand("김뿌꾸", BigDecimal.valueOf(1000000));
        User user = userService.createUser(command);

        // 2. JPA로 조회
        User fetched = userService.getUser(user.getId());
    }

    @Test
    @DisplayName("유저의 잔액을 충전할 수 있다.")
    void userBalanceIncrease() {

        //Given
        UserCreateCommand command = createCommand("김뿌꾸", BigDecimal.valueOf(500));
        User user = userService.createUser(command);

        //When
        userService.increaseBalance(user.getId(),BigDecimal.valueOf(10000));

        //Then
        Assertions.assertAll(
                () -> assertThat(user.getId()).isNotNull(),
                () ->  assertThat(user.getBalance()).isEqualTo(BigDecimal.valueOf(10500))
        );

    }
}