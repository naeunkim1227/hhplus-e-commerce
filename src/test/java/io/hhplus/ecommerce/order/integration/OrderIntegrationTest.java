package io.hhplus.ecommerce.order.integration;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.infrastructure.jpa.JpaCartRepository;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaUserCouponRepository;
import io.hhplus.ecommerce.fixture.CouponFixture;
import io.hhplus.ecommerce.fixture.ProductFixture;
import io.hhplus.ecommerce.fixture.UserFixture;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.application.usecase.OrderCreateFromCartUseCase;
import io.hhplus.ecommerce.order.application.usecase.OrderGetUseCase;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.infrastructure.repositoty.jpa.JpaOrderRepository;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import io.hhplus.ecommerce.product.infrastructure.repositoty.jpa.JpaProductRepository;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.infrastructure.repositoty.jpa.JpaUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@DisplayName("Order 통합 테스트 - UseCase + Service + Repository + DB")
public class OrderIntegrationTest {

    @Autowired
    private OrderGetUseCase orderGetUseCase;

    @Autowired
    private OrderCreateFromCartUseCase orderCreateFromCartUseCase;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private JpaCouponRepository jpaCouponRepository;

    @Autowired
    private JpaCartRepository jpaCartRepository;

    @Autowired
    private JpaUserCouponRepository jpaUserCouponRepository;

    User savedUser;
    Product savedProduct;
    Coupon savedCoupon;
    CartItem savedCartItem;
    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @BeforeEach
    void setUp() {
        //유저 생성
        User user = UserFixture.defaultUser();
        savedUser = jpaUserRepository.save(user);  // 저장 후 생성된 ID를 받아옴
        jpaUserRepository.flush();

        //쿠폰 생성
        Coupon coupon = CouponFixture.defaultCoupon();
        savedCoupon = jpaCouponRepository.save(coupon);
        jpaCouponRepository.flush();

        //유저 쿠폰 생성
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(savedUser.getId())
                .couponId(savedCoupon.getId())
                .build();
        jpaUserCouponRepository.save(userCoupon);
        jpaUserCouponRepository.flush();

        //상품 생성
        Product product = ProductFixture.defaultProduct();
        savedProduct = jpaProductRepository.save(product);
        jpaProductRepository.flush();

        //카트 생성 - 실제 저장된 user와 product의 ID 사용
        CartItem cartItem = CartItem.builder()
                .userId(savedUser.getId())  // 실제 저장된 userId
                .productId(savedProduct.getId())  // 실제 저장된 productId
                .quantity(2)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        savedCartItem = jpaCartRepository.save(cartItem);
        jpaCartRepository.flush();
    }

    @AfterEach
    void tearDown() {
        // 자식 엔티티부터 삭제 (외래 키 제약 조건)
        jpaOrderRepository.deleteAll();  // Order & OrderItem (cascade)
        jpaCartRepository.deleteAll();
        jpaUserCouponRepository.deleteAll();  // User, Coupon 참조
        jpaProductRepository.deleteAll();
        jpaCouponRepository.deleteAll();
        jpaUserRepository.deleteAll();
    }

    @Test
    @DisplayName("장바구니에 있는 상품을 주문 및 조회 - 쿠폰 미사용")
    void createCartOrderAndGetOrder() {
        //Given
        OrderCreateFromCartCommand command =
                OrderCreateFromCartCommand.builder()
                        .userId(savedUser.getId())
                        .cartItemIds(List.of(savedCartItem.getId()))
                        .build();
        //When
        OrderDto orderDto = orderCreateFromCartUseCase.excute(command);

        //Then
        Assertions.assertAll(
                "쿠폰 미사용 주문 생성 검증",
                () -> assertThat(orderDto).isNotNull(),
                () -> assertThat(orderDto.getUserId()).isEqualTo(savedUser.getId()),
                () -> assertThat(orderDto.getFinalAmount())
                        .isEqualTo(savedProduct.getPrice().multiply(BigDecimal.valueOf(savedCartItem.getQuantity()))),
                () -> assertThat(orderDto.getOrderItems()).hasSize(1),
                () -> assertThat(orderDto.getOrderItems().get(0).getProductId())
                        .isEqualTo(savedProduct.getId()),
                () -> assertThat(orderDto.getOrderItems().get(0).getQuantity())
                        .isEqualTo(savedCartItem.getQuantity())
        );
    }


    @Test
    @DisplayName("장바구니에 있는 상품을 주문 및 조회 - 쿠폰 사용")
    void createCartOrderAndGetOrderWithCoupon() {
        //Given
        OrderCreateFromCartCommand command =
                OrderCreateFromCartCommand.builder()
                        .userId(savedUser.getId())
                        .couponId(savedCoupon.getId())
                        .cartItemIds(List.of(savedCartItem.getId()))
                        .build();

        //When
        OrderDto orderDto = orderCreateFromCartUseCase.excute(command);

        BigDecimal amount = savedProduct.getPrice().multiply(BigDecimal.valueOf(savedCartItem.getQuantity()));
        BigDecimal discountAmount = amount.multiply(savedCoupon.getDiscountRate())
                .divide(BigDecimal.valueOf(100), RoundingMode.DOWN);

        //Then

        Assertions.assertAll(
                "쿠폰 사용 주문 생성 검증",
                () -> assertThat(orderDto).isNotNull(),
                () -> assertThat(orderDto.getUserId()).isEqualTo(savedUser.getId()),
                () -> assertThat(orderDto.getFinalAmount())
                        .isEqualTo(amount.subtract(discountAmount)),
                () -> assertThat(orderDto.getOrderItems()).hasSize(1),
                () -> assertThat(orderDto.getOrderItems().get(0).getProductId())
                        .isEqualTo(savedProduct.getId()),
                () -> assertThat(orderDto.getOrderItems().get(0).getQuantity())
                        .isEqualTo(savedCartItem.getQuantity())
        );
    }


    @Test
    @DisplayName("30000원 이하 주문에 FIXED 쿠폰 적용 시 실패 - 최소 주문금액 미달")
    void createCartOrderAndFailOrderWithCoupon() {
        // Given
        // 저가 상품
        Product lowPriceProduct = Product.builder()
                .name("초콜릿")
                .price(BigDecimal.valueOf(10000))
                .stock(150L)
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        Product savedLowProduct = jpaProductRepository.save(lowPriceProduct);
        jpaProductRepository.flush();

        // 장바구니
        CartItem lowPriceCartItem = CartItem.builder()
                .userId(savedUser.getId())
                .productId(savedLowProduct.getId())
                .quantity(2)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        CartItem savedLowCartItem = jpaCartRepository.save(lowPriceCartItem);
        jpaCartRepository.flush();

        // 3. 쿠폰 생성
        Coupon fixedCoupon = Coupon.builder()
                .code("FIXED10")
                .name("10000원 할인 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(5)
                .startDate(LocalDateTime.now())
                .status(CouponStatus.ACTIVE)
                .type(CouponType.FIXED)
                .discountRate(BigDecimal.valueOf(10000))
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Coupon savedFixedCoupon = jpaCouponRepository.save(fixedCoupon);
        jpaCouponRepository.flush();

        // 4. 유저에게 쿠폰 발급
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(savedUser.getId())
                .couponId(savedFixedCoupon.getId())
                .usedAt(null)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .build();
        jpaUserCouponRepository.save(userCoupon);
        jpaUserCouponRepository.flush();

        //주문
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(savedUser.getId())
                .couponId(savedFixedCoupon.getId())  // FIXED 타입 쿠폰 (최소 30000원 필요)
                .cartItemIds(List.of(savedLowCartItem.getId()))
                .build();

        // When & Then: 주문 금액(20000원) < 최소 주문 금액(30000원) → 예외 발생
        Assertions.assertThrows(
                BusinessException.class,
                () -> orderCreateFromCartUseCase.excute(command),
                CouponErrorCode.COUPON_MINIMUM_AMOUNT_NOT_MET.getMessage()
        );
    }

    @Test
    @DisplayName("주문 생성 후 주문을 조회 - Fetch Join")
    void findOrderWithOrderItems() {
        // Given: 주문 생성
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(savedUser.getId())
                .cartItemIds(List.of(savedCartItem.getId()))
                .build();
        OrderDto orderDto = orderCreateFromCartUseCase.excute(command);

        // When: Order와 OrderItem을 함께 조회
        OrderDto order = orderGetUseCase.excute(orderDto.getId());

        // Then
        Assertions.assertAll(
                "Order와 OrderItem 함께 조회 검증",
                () -> assertThat(order.getId()).isEqualTo(orderDto.getId()),
                () -> assertThat(order.getOrderItems()).hasSize(1),
                () -> assertThat(order.getOrderItems().get(0).getProductId())
                        .isEqualTo(savedProduct.getId()),
                () -> assertThat(order.getOrderItems().get(0).getQuantity())
                        .isEqualTo(savedCartItem.getQuantity())
        );
    }






}
