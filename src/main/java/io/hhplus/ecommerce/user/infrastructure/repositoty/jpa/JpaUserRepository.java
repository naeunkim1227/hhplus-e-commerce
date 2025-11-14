package io.hhplus.ecommerce.user.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {

}
