package io.hhplus.ecommerce.user.integration;

import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.domain.service.UserService;
import io.hhplus.ecommerce.user.infrastructure.repositoty.jpa.JpaUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@DisplayName("user  통합 테스트")
public class UserIntegrationTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;
    @Autowired
    private UserService userService;

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
                () ->  assertThat(updatedUser.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(60000))
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


    @Test
    @DisplayName("잔액 차감 동시성 테스트 - 10개 스레드가 동시에 1000원씩 차감")
    void decreaseBalanceConcurrency() throws InterruptedException {
        // Given
        int userBalance = 10000;
        User user = createUser("박물개", 10000);
        User savedUser = jpaUserRepository.save(user);
        jpaUserRepository.flush();
        Long userId = savedUser.getId();

        int threadCount = 5;  // 10개 동시 스레드
        int decreaseAmount = 1000;

        ExecutorService executorService = newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When: 100개 스레드가 동시에 100원씩 차감
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    userService.reduceBalance(userId, BigDecimal.valueOf(decreaseAmount));
                    Thread.sleep(300);

                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        User resultUser = jpaUserRepository.findById(userId).orElseThrow();
        assertThat(resultUser.getBalance()).isEqualByComparingTo(String.valueOf(userBalance - (decreaseAmount * threadCount)));
    }


}