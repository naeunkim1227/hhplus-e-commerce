package io.hhplus.ecommerce.user.entity;

import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.infrastructure.repositoty.jpa.JpaUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("default")
@DisplayName("Docker MySQL 엔티티 테스트")
public class UserJpaTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @BeforeEach
    void setUp() {
        jpaUserRepository.deleteAll();
    }

    @Test
    @DisplayName("User 저장 및 조회 테스트")
    void saveAndFindUser() {
        // Given
        User user = createUser("김문어", 50000);

        // When
        User savedUser = jpaUserRepository.save(user);
        jpaUserRepository.flush();

        // Then: 저장된 User 조회
        User foundUser = jpaUserRepository.findById(savedUser.getId()).orElseThrow();

        Assertions.assertAll
                ("유저 정보 검증",
                        () -> assertThat(foundUser.getId()).isNotNull(),
                        () -> assertThat(foundUser.getName()).isEqualTo("김문어"),
                        () -> assertThat(foundUser.getBalance()).isEqualTo(50000)
                );
    }

    @Test
    @DisplayName("여러 User 저장 및 조회")
    void saveMultipleUsers() {
        // Given
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
                        .contains("김문어", "김굴비", "김홍합"));
    }


    @Test
    @DisplayName("User 업데이트")
    void updateUser() {
        // Given
        User user = createUser("김문어", 50000);
        User savedUser = jpaUserRepository.save(user);
        Long userId = savedUser.getId();

        // When
        User selectUser = jpaUserRepository.findById(userId).orElseThrow();
        selectUser.addBalance(BigDecimal.valueOf(10000));
        jpaUserRepository.save(selectUser);

        // Then
        User updatedUser = jpaUserRepository.findById(userId).orElseThrow();
        Assertions.assertAll(
                () -> assertThat(updatedUser).isNotNull(),
                () -> assertThat(updatedUser.getId()).isEqualTo(userId),
                () ->  assertThat(updatedUser.getBalance()).isEqualTo(BigDecimal.valueOf(60000))
        );
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