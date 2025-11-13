package io.hhplus.ecommerce.user.domain;

import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;

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
}