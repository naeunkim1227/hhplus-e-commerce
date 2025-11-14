package io.hhplus.ecommerce.product.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 단위 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private Product product3;

    private List<Product> productList;

    @BeforeEach
    void setUp() {
        // Given
        //판매가능
        product1 = Product.builder()
                .id(1L)
                .name("프레첼")
                .price(new BigDecimal("129000"))
                .stock(150L)
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        product2 = Product.builder()
                .id(2L)
                .name("아메리카노")
                .price(new BigDecimal("450000"))
                .stock(80L)
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        //판매중지
        product3 = Product.builder()
                .id(3L)
                .name("에어팟")
                .price(new BigDecimal("35000"))
                .stock(200L)
                .status(ProductStatus.INACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productList = Arrays.asList(product1, product2, product3);
    }

    @Test
    @DisplayName("존재하는 상품 ID로 조회하면 상품을 반환한다")
    void getProduct_Success() {
        // Given: 상품 ID가 주어지고 Repository가 상품을 반환하도록 설정
        Long productId = 1L;
        given(productRepository.findById(productId)).willReturn(Optional.of(product1));

        // When: 상품 조회
        Product product = productService.getProduct(productId);

        // Then: 상품이 조회되고 Repository 호출 검증
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getName()).isEqualTo("프레첼");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회하면 예외가 발생한다")
    void getProduct_WithInvalidId_ThrowsException() {
        // Given: 존재하지 않는 상품 ID
        Long notExistProduct = 999L;
        given(productRepository.findById(notExistProduct)).willReturn(Optional.empty());

        // When & Then: 조회 시 예외 발생 검증
        assertThatThrownBy(() -> productService.getProduct(notExistProduct))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());

        verify(productRepository, times(1)).findById(notExistProduct);
    }

    @Test
    @DisplayName("상품 목록을 조회한다")
    void getAllProducts_Success() {
        given(productRepository.findAll()).willReturn(productList);

        // When: 전체 상품 조회
        List<Product> products = productService.getAllProducts();

        // Then: 모든 상품이 조회되고 Repository 호출 검증
        assertThat(products).isNotEmpty();
        assertThat(products).hasSize(3);

        verify(productRepository, times(1)).findAll();
    }

}
