package io.hhplus.ecommerce.cart.integration;


import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.application.usecase.*;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.product.infrastructure.repositoty.jpa.JpaProductRepository;
import io.hhplus.ecommerce.user.domain.entity.User;
import io.hhplus.ecommerce.user.infrastructure.repositoty.jpa.JpaUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Import(TestContainerConfig.class)
@DisplayName("Cart 통합 테스트 - UseCase + Service + Repository + DB")
class CartIntegrationTest {

    @Autowired
    CartAddUseCase cartAddUseCase;

    @Autowired
    CartUpdateUseCase cartUpdateUseCase;

    @Autowired
    JpaProductRepository jpaProductRepository;

    @Autowired
    JpaUserRepository  jpaUserRepository;

    List<Product> productList = new ArrayList<>();
    User user;

    @BeforeEach
    void setUp() {
        productList.clear();

        //유저 생성
        user = User.builder()
                .name("김뿌꾸")
                .balance(BigDecimal.valueOf(10000L))
                .build();

        user = jpaUserRepository.save(user);
        jpaUserRepository.flush();

        //상품 생성
        Product product1 = createProduct("마라탕", 10L, 4500);
        Product product2 = createProduct("양꼬치", 10L, 5500);

        Product savedProduct1 = jpaProductRepository.save(product1);
        Product savedProduct2 = jpaProductRepository.save(product2);
        jpaProductRepository.flush();

        productList.add(savedProduct1);
        productList.add(savedProduct2);
    }

    @Test
    @DisplayName("재고가 없는 상품을 추가하려고 할때 실패한다.")
    @Transactional
    void addCartWhenInsufficientStock() {
        //Given: 테스트 내부에서 데이터 생성
        User testUser = jpaUserRepository.save(User.builder()
                .name("테스트유저")
                .balance(BigDecimal.valueOf(10000L))
                .build());

        Product testProduct = jpaProductRepository.save(createProduct("테스트상품", 10L, 5000));
        jpaProductRepository.flush();

        CartItemAddCommand command = CartItemAddCommand.builder()
                        .quantity(100)  // 재고 10개인데 100개 요청
                        .productId(testProduct.getId())
                        .userId(testUser.getId())
                        .build();

        //When & Then
        try {
            CartItemDto result = cartAddUseCase.execute(command);
            fail("예외가 발생해야 하는데 성공했습니다. result: " + result);
        } catch (Exception e) {
            System.out.println("발생한 예외: " + e.getClass().getName());
            System.out.println("예외 메시지: " + e.getMessage());
            if (e instanceof BusinessException) {
                BusinessException be = (BusinessException) e;
                System.out.println("에러 코드: " + be.getErrorCode());
            }
            assertThat(e).isInstanceOf(BusinessException.class);
            assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }


    @Test
    @DisplayName("장바구니 수량을 변경할 수 있다.")
    @Transactional
    void changeCartQuantity() {
        //Given 카트 상품 추가
        CartItemAddCommand addCommand = CartItemAddCommand.builder()
                .quantity(1)
                .productId(productList.get(0).getId())
                .userId(user.getId())
                .build();

        CartItemDto addCart = cartAddUseCase.execute(addCommand);

        //When
        CartItemUpdateCommand updateCommand = CartItemUpdateCommand.builder()
                .cartItemId(addCart.getCartItemId())
                .userId(user.getId())
                .quantity(2)
                .build();

        CartItemDto updateCart = cartUpdateUseCase.excute(updateCommand);

        //Then
        Assertions.assertAll(
                () -> assertThat(updateCart.getCartItemId()).isEqualTo(addCart.getCartItemId()),
                () -> assertThat(updateCart.getQuantity()).isEqualTo(3)
        );
    }


    //재고 검증을 위한 product 생성
    private Product createProduct(String name, Long stock, int price) {
        return Product.builder()
                .name(name)
                .stock(stock)
                .status(ProductStatus.ACTIVE)
                .price(BigDecimal.valueOf(price))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
