package io.hhplus.ecommerce.order.domain.service;

import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import io.hhplus.ecommerce.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
@DisplayName( "OrderService 단위 Test")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private User user;
    private Product product;

    @InjectMocks
    private OrderService orderService;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .name("김뿌꾸")
                .balance(BigDecimal.valueOf(10000))
                .build();


        product = Product.builder()
                .id(1L)
                .name("프레첼")
                .price(new BigDecimal("129000"))
                .stock(150L)
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
