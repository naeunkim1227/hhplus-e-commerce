package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
    private final ProductService productService;

    public ProductDto execute(@Valid ProductCreateCommand command) {
        Product product = productService.createProduct(command);
        return ProductDto.from(product);
    }
}
