package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductListUseCase {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (페이징 및 필터링 포함)
     */
    public List<ProductDto> execute() {
        List<Product> products = productService.getAllProducts();
        return products.stream()
                .map(ProductDto::from)
                .toList();
    }

}