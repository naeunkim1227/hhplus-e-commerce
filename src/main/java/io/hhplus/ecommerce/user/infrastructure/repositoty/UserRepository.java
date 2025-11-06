package io.hhplus.ecommerce.user.infrastructure.repositoty;

import io.hhplus.ecommerce.user.domain.entity.User;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
}