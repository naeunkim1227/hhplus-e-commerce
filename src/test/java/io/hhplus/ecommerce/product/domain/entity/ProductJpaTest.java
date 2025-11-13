package io.hhplus.ecommerce.product.domain.entity;


import io.hhplus.ecommerce.config.TestContainerConfig;
import io.hhplus.ecommerce.product.infrastructure.repositoty.jpa.JpaProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@DisplayName("Testcontainers를 사용한 Product integration 테스트")
class ProductJpaTest {

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Test
    @DisplayName("Product 저장 및 조회")
    @Transactional
    void saveAndFindProduct() {
        // Given
        Product product = createProduct("라떼", 10L, 5000);

        // When
        Product savedProduct = jpaProductRepository.save(product);
        jpaProductRepository.flush();

        // Then
        Product foundProduct = jpaProductRepository.findById(savedProduct.getId()).orElseThrow();

        Assertions.assertAll(
                "상품 정보 검증",
                () -> assertThat(foundProduct.getId()).isNotNull(),
                () -> assertThat(foundProduct.getName()).isEqualTo("라떼"),
                () -> assertThat(foundProduct.getStock()).isEqualTo(10L),
                () -> assertThat(foundProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(5000))
        );
    }

    @Test
    @DisplayName("여러 Product 저장 및 모든 Product 조회")
    @Transactional
    void saveMultipleAndFindAllProducts() {
        // Given: 3개의 Product 저장
        Product product1 = createProduct("아메리카노", 50L, 4500);
        Product product2 = createProduct("카페라떼", 30L, 5000);
        Product product3 = createProduct("카푸치노", 20L, 5500);

        jpaProductRepository.save(product1);
        jpaProductRepository.save(product2);
        jpaProductRepository.save(product3);

        // When
        var products = jpaProductRepository.findAll();


        // Then
        Assertions.assertAll(
                () -> assertThat(products).isNotNull(),
                () -> assertThat(products).hasSize(3),
                () -> assertThat(products).extracting(Product::getName)
                        .contains("아메리카노", "카페라떼", "카푸치노")
        );
    }


    @Test
    @DisplayName("여러 Product 저장 및 조회")
    @Transactional
    void saveMultipleProducts() {
        // Given: 3개의 Product 저장
        Product product1 = createProduct("아메리카노", 50L, 4500);
        Product product2 = createProduct("카페라떼", 30L, 5000);
        Product product3 = createProduct("카푸치노", 20L, 5500);

        jpaProductRepository.save(product1);
        jpaProductRepository.save(product2);
        jpaProductRepository.save(product3);

        // When
        var selectedIds = java.util.List.of(product1.getId(), product2.getId(), product3.getId());
        var selectedProducts = jpaProductRepository.findAllById(selectedIds);

        // Then
        Assertions.assertAll(
                () -> assertThat(selectedProducts).isNotNull(),
                () -> assertThat(selectedProducts).hasSize(3),
                () -> assertThat(selectedProducts).extracting(Product::getName)
                        .contains("아메리카노", "카페라떼", "카푸치노")
        );
    }

    @Test
    @DisplayName("Product 재고 업데이트")
    void updateProductStock() {
        // Given
        Product product = createProduct("에스프레소", 100L, 3000);
        Product savedProduct = jpaProductRepository.save(product);
        jpaProductRepository.flush();
        Long productId = savedProduct.getId();

        // When: 재고 감소
        Product foundProduct = jpaProductRepository.findById(productId).orElseThrow();
        foundProduct.decreaseStock(30);
        jpaProductRepository.save(foundProduct);
        jpaProductRepository.flush();

        // Then
        Product updatedProduct = jpaProductRepository.findById(productId).orElseThrow();
        Assertions.assertAll(
                () -> assertThat(updatedProduct).isNotNull(),
                () -> assertThat(updatedProduct.getId()).isEqualTo(productId),
                () -> assertThat(updatedProduct.getStock()).isEqualTo(70L)
        );
    }

    @Test
    @DisplayName("Product 삭제")
    @Transactional
    void deleteProduct() {
        // Given
        Product product = createProduct("콜드브루", 15L, 6000);
        Product savedProduct = jpaProductRepository.save(product);
        jpaProductRepository.flush();
        Long productId = savedProduct.getId();

        // When
        jpaProductRepository.deleteById(productId);

        // Then: 조회 안됨
        assertThat(jpaProductRepository.findById(productId)).isEmpty();
    }

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
