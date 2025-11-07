package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
    private final ProductService productService;
    /**
     * 상품 상세 조회
     */
    public ProductDto execute(Long productId) {
        Product product = productService.getProduct(productId);
        return ProductDto.from(product);
    }
}
