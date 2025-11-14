package io.hhplus.ecommerce.user.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.user.application.dto.command.UserCreateCommand;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.domain.exception.UserErrorCode;
import io.hhplus.ecommerce.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(UserCreateCommand command) {
        User user = User.create(command);
        return userRepository.save(user);
    }

    /**
     * 유저 조회
     */
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 잔액 차감
     */
    public void reduceBalance(Long userId, BigDecimal amount) {
        User user = this.getUser(userId);
        user.reduceBalance(amount);
        userRepository.save(user);
    }

}
