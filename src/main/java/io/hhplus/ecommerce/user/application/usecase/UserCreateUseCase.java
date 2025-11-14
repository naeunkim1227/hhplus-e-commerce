package io.hhplus.ecommerce.user.application.usecase;

import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import io.hhplus.ecommerce.user.application.dto.result.UserDto;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreateUseCase {
    private final UserService userService;

    public UserDto execute(@Valid UserCreateCommand command) {
        User user = userService.createUser(command);
        return UserDto.from(user);
    }

}
