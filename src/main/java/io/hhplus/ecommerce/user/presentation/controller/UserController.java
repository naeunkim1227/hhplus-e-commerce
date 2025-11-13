package io.hhplus.ecommerce.user.presentation.controller;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.user.application.dto.result.UserDto;
import io.hhplus.ecommerce.user.application.usecase.UserCreateUseCase;
import io.hhplus.ecommerce.user.presentation.dto.request.UserCreateRequest;
import io.hhplus.ecommerce.user.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserCreateUseCase userCreateUseCase;

    @Operation(summary = "유저 생성", description = "유저를 생성합니다.")
    @GetMapping
    public CommonResponse<UserResponse> createUser(@Valid @ModelAttribute UserCreateRequest request){
        UserDto userDto = userCreateUseCase.execute(request.toCommand());
        return CommonResponse.success(UserResponse.from(userDto));
    }


}
