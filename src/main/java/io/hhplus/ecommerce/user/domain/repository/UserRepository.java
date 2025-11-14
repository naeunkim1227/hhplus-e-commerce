package io.hhplus.ecommerce.user.domain.repository;

import io.hhplus.ecommerce.user.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
}