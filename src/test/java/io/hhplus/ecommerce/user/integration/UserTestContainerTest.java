package io.hhplus.ecommerce.user.integration;

import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.infrastructure.repositoty.jpa.JpaUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@DisplayName("Testcontainers JPA 레이어 테스트")
class UserTestContainerTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    @DisplayName("User 저장 및 조회")
    void saveAndFindUser() {
        // Given
        User user = createUser("김문어", 50000);

        // When
        User saveUser = jpaUserRepository.save(user);
        jpaUserRepository.flush();

        // Then
        Assertions.assertAll
                ("유저 정보 검증",
                () -> assertThat(saveUser.getId()).isNotNull(),
                () -> assertThat(saveUser.getName()).isEqualTo("김문어"),
                () -> assertThat(saveUser.getBalance()).isEqualTo(BigDecimal.valueOf(50000))
        );
    }

    @Test
    @DisplayName("여러 User 저장 및 조회")
    void saveMultipleUsers() {
        // Given: 3명의 User 저장
        User user1 = createUser("김문어", 10000);
        User user2 = createUser("김굴비", 20000);
        User user3 = createUser("김홍합", 30000);

        jpaUserRepository.save(user1);
        jpaUserRepository.save(user2);
        jpaUserRepository.save(user3);
        jpaUserRepository.flush();

        // When
        var manyUsers = jpaUserRepository.findAll();

        // Then
        Assertions.assertAll(
                () -> assertThat(manyUsers).isNotNull(),
                () -> assertThat(manyUsers).hasSize(3),
                () -> assertThat(manyUsers).extracting(User::getName)
                .contains("김문어", "김굴비", "김홍합")
        );
    }

    @Test
    @DisplayName("User 업데이트")
    void updateUser() {
        // Given
        User user = createUser("김문어", 50000);
        User savedUser = jpaUserRepository.save(user);
        jpaUserRepository.flush();
        Long userId = savedUser.getId();

        // When
        User selectUser = jpaUserRepository.findById(userId).orElseThrow();
        selectUser.addBalance(BigDecimal.valueOf(10000));
        jpaUserRepository.save(selectUser);
        jpaUserRepository.flush();

        // Then
        User updatedUser = jpaUserRepository.findById(userId).orElseThrow();
        Assertions.assertAll(
                () -> assertThat(updatedUser).isNotNull(),
                () -> assertThat(updatedUser.getId()).isEqualTo(userId),
                () ->  assertThat(updatedUser.getBalance()).isEqualTo(BigDecimal.valueOf(60000))
        );
    }

    @Test
    @DisplayName("User 삭제 ")
    void deleteUser() {
        // Given
        User user = createUser("박물개", 1000);
        User savedUser = jpaUserRepository.save(user);
        jpaUserRepository.flush();
        Long userId = savedUser.getId();

        // When
        jpaUserRepository.deleteById(userId);


        jpaUserRepository.deleteById(userId);
        jpaUserRepository.flush();

        // Then: 조회 안됨
        assertThat(jpaUserRepository.findById(userId)).isEmpty();
    }

    private User createUser(String name, int balance) {
        return User.builder()
                .name(name)
                .balance(BigDecimal.valueOf(balance))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}