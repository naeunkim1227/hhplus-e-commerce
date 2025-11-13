package io.hhplus.ecommerce.product.integration;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.application.dto.command.ProductPopularCommand;
import io.hhplus.ecommerce.product.application.usecase.ProductCreateUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductGetUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductListUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductPopularUseCase;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;

import io.hhplus.ecommerce.product.infrastructure.repositoty.jpa.JpaProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@DisplayName("Product 통합 테스트 - UseCase + Service + Repository + DB")
class ProductIntegrationTest {

    @Autowired
    private ProductCreateUseCase productCreateUseCase;

    @Autowired
    private ProductListUseCase productListUseCase;

    @Autowired
    private ProductGetUseCase ProductGetUseCase;

    @Autowired
    private ProductPopularUseCase productPopularUseCase;
    @Autowired
    private JpaProductRepository jpaProductRepository;


    @Test
    @DisplayName("상품 전체 조회")
    void listAllProducts() {
        // Given: 상품 3개 생성
        productCreateUseCase.execute(createCommand("상품1", 1000, 50L));
        productCreateUseCase.execute(createCommand("상품2", 2000, 100L));
        productCreateUseCase.execute(createCommand("상품3", 3000, 150L));

        // When
        List<ProductDto> productDtoList =  productListUseCase.execute();

        // Then
        Assertions.assertAll(
                "상품 전체 조회",
                () -> assertThat(productDtoList).isNotNull(),
                () -> assertThat(productDtoList).hasSizeGreaterThanOrEqualTo(3)
        );
    }


    @Test
    @DisplayName("상품 생성 및 조회")
    void createAndGetProduct() {
        // Given
        ProductCreateCommand command  = createCommand("꿀아메리카노", 4500, 100L);

        // When
        ProductDto createdProduct = productCreateUseCase.execute(command);

        // Then
        ProductDto selectedProduct = ProductGetUseCase.execute(createdProduct.getId());
        Assertions.assertAll(
                "상품 생성 검증",
                () -> assertThat(createdProduct.getId()).isNotNull(),
                () -> assertThat(createdProduct.getPrice()).isEqualByComparingTo(new BigDecimal("4500")),
                () -> assertThat(createdProduct.getStock()).isEqualTo(100L),
                () -> assertThat(selectedProduct.getName()).isEqualTo("꿀아메리카노")
        );
    }

    @Test
    @DisplayName("여러 상품 생성 후 목록 조회")
    void createMultipleProductsAndList() {
        // Given: 3개의 상품 생성
        ProductCreateCommand command1 = createCommand("아메리카노", 4500, 100L);
        ProductCreateCommand command2 = createCommand("카페라떼", 5000, 80L);
        ProductCreateCommand command3 = createCommand("카푸치노", 5500, 60L);

        productCreateUseCase.execute(command1);
        productCreateUseCase.execute(command2);
        productCreateUseCase.execute(command3);

        // When
        List<ProductDto> products = productListUseCase.execute();

        // Then
        Assertions.assertAll(
                () -> assertThat(products).hasSizeGreaterThanOrEqualTo(3),
                () -> assertThat(products).extracting(ProductDto::getName)
                        .contains("아메리카노", "카페라떼", "카푸치노")
        );
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외 발생")
    void throwExceptionWhenProductNotFound() {
        // Given
        Long notExistProductId = 99999L;

        // When/Then
        assertThatThrownBy(() -> ProductGetUseCase.execute(notExistProductId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @Sql(scripts = {
            "/sql/cleanup.sql",
            "/sql/test_users.sql",
            "/sql/test_products.sql",
            "/sql/test_coupons.sql",
            "/sql/test_user_coupons.sql",
            "/sql/test_orders.sql"
    })
    @DisplayName("인기 상품 조회 - 정상 케이스")
    void getPopularProducts() {
        // Given
        ProductPopularCommand command = ProductPopularCommand.builder()
                .days(3)
                .limit(5)
                .build();

        // When
        List<ProductDto> popularProducts = productPopularUseCase.execute(command);

        // Then
        Assertions.assertAll(
                "인기 상품 조회 검증",
                () -> assertThat(popularProducts).isNotNull(),
                () -> assertThat(popularProducts.size()).isLessThanOrEqualTo(5)
        );
    }

    @Test
    @DisplayName("인기 상품 조회 - days가 최대 검색 기간(100일)을 초과하면 예외 발생")
    void getfindPopularProductsWhenDaysExceed100Days() {
        // Given
        ProductPopularCommand command = ProductPopularCommand.builder()
                .days(101)
                .limit(5)
                .build();

        // When/Then
        assertThatThrownBy(() -> productPopularUseCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ProductErrorCode.SEARCH_CONDITION_LIMIT.getMessage());
    }

    @Test
    @DisplayName("인기 상품 조회 - limit이 정상 적용되는지 확인")
    @Sql(scripts = {
            "/sql/cleanup.sql",
            "/sql/test_users.sql",
            "/sql/test_products.sql",
            "/sql/test_coupons.sql",
            "/sql/test_user_coupons.sql",
            "/sql/test_orders.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getPopularProductsWithLimit() {
        // Given
        List<Product> allProducts = jpaProductRepository.findAll();


        ProductPopularCommand command = ProductPopularCommand.builder()
                .days(30)
                .limit(3)
                .build();

        // When
        List<ProductDto> popularProducts = productPopularUseCase.execute(command);


        // Then
        Assertions.assertAll(
                "limit 적용 검증",
                () -> assertThat(popularProducts).isNotNull(),
                () -> assertThat(popularProducts.size()).isLessThanOrEqualTo(3)
        );
    }

    private ProductCreateCommand createCommand(String name, int price, Long stock) {
        return ProductCreateCommand.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stock(stock)
                .build();
    }
}