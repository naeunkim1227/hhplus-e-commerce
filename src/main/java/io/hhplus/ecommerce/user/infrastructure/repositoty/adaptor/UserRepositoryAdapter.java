package io.hhplus.ecommerce.user.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.domain.repository.UserRepository;
import io.hhplus.ecommerce.user.infrastructure.repositoty.jpa.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }
}