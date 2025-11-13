package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.product.application.dto.command.ProductPopularCommand;
import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.product.domain.validator.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPopularUseCase {
    private final ProductService productService;

    private final ProductValidator productValidator;

    /**
     * 인기 상품 조회
     */
    public List<ProductDto> execute(ProductPopularCommand command) {
        //검색 조건 검증
        productValidator.validate(command.getDays());

        List<Product> products = productService.getPopularProducts(command);
        return products.stream()
                .map(ProductDto::from)
                .toList();
    }
}
