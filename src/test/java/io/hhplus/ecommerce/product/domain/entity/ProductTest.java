package io.hhplus.ecommerce.product.domain.entity;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 엔티티 단위 테스트")
class ProductTest {

    private ProductCreateCommand createCommand(String name, BigDecimal price, Long stock) {
        return ProductCreateCommand.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
    }

    @Test
    @DisplayName("상품 생성 성공")
    void create_Product_Success() {
        // Given
        ProductCreateCommand command = createCommand("노트북", new BigDecimal("1000000"), 10L);

        // When
        Product product = Product.create(command);

        // Then
        assertThat(product.getName()).isEqualTo("노트북");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(product.getStock()).isEqualTo(10L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("재고를 정상적으로 감소시킨다")
    void decreaseStock_Success() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When
        product.decreaseStock(5);

        // Then
        assertThat(product.getStock()).isEqualTo(5L);
    }

    @Test
    @DisplayName("재고보다 많은 수량을 감소시키면 예외가 발생한다")
    void decreaseStock_Fail_OverStock() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When & Then
        assertThatThrownBy(() -> product.decreaseStock(15))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("0 이하의 수량으로 재고 감소 시 예외가 발생한다")
    void decreaseStock_Fail_MinusStock() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When & Then
        assertThatThrownBy(() -> product.decreaseStock(0))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INVALID_QUANTITY);

        assertThatThrownBy(() -> product.decreaseStock(-1))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INVALID_QUANTITY);
    }

    @Test
    @DisplayName("재고를 정상적으로 증가시킨다")
    void increaseStock_Success() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When
        product.increaseStock(5);

        // Then
        assertThat(product.getStock()).isEqualTo(15L);
    }

    @Test
    @DisplayName("0 이하의 수량으로 재고 증가 시 예외가 발생한다")
    void increaseStock_Fail_MinusStock() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When & Then
        assertThatThrownBy(() -> product.increaseStock(0))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INVALID_QUANTITY);
    }

    @Test
    @DisplayName("상품 상태를 정상적으로 변경한다")
    void changeStatus_Success() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When
        product.changeStatus(ProductStatus.INACTIVE);

        // Then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }


    @Test
    @DisplayName("재고가 충분하면 true를 반환한다")
    void product_valid_success() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 100L);
        Product product = Product.create(command);

        // When & Then
        assertThat(product.isStockAvailable(5)).isTrue();
        assertThat(product.isStockAvailable(10)).isTrue();
    }

    @Test
    @DisplayName("재고가 부족하면 false를 반환한다")
    void product_valid_fail() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When & Then
        assertThat(product.isStockAvailable(11)).isFalse();
    }

    @Test
    @DisplayName("상태가 ACTIVE가 아니면 판매가 불가능하다")
    void isAvailableForSale_WithActiveStatusAndStock_ReturnsTrue() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 10L);
        Product product = Product.create(command);

        // When & Then
        assertThat(product.isAvailableForSale()).isTrue();
    }


    @Test
    @DisplayName("재고가 0이면 판매 불가능하다")
    void isAvailableForSale_WithZeroStock_ReturnsFalse() {
        // Given
        ProductCreateCommand command = createCommand("상품", new BigDecimal("1000"), 0L);
        Product product = Product.create(command);

        // When & Then
        assertThat(product.isAvailableForSale()).isFalse();
    }
}