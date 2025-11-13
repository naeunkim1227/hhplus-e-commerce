package io.hhplus.ecommerce.order.integration;


import io.hhplus.ecommerce.order.infrastructure.repositoty.jpa.JpaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("default")
@DisplayName("Docker MySQL 통합 테스트")
public class OrderJpaTest {

    @Autowired
    private JpaOrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }


    //    @Test
//    @DisplayName()
//    @Transactional
//    void


}
