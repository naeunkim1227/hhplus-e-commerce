package io.hhplus.ecommerce.product.presentation.controller;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.application.usecase.ProductCreateUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductListUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductGetUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductPopularUseCase;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.product.presentation.dto.request.ProductCreateRequest;
import io.hhplus.ecommerce.product.presentation.dto.request.ProductPopularRequest;
import io.hhplus.ecommerce.product.presentation.dto.request.ProductSearchRequest;
import io.hhplus.ecommerce.product.presentation.dto.request.ProductUpdateRequest;
import io.hhplus.ecommerce.product.presentation.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductGetUseCase productGetUseCase;
    private final ProductListUseCase productListUseCase;
    private final ProductCreateUseCase productCreateUseCase;
    private final ProductPopularUseCase productPopularUseCase;
    private final ProductService productService;

    /**
     *  상품 목록 조회
     */
    @GetMapping
    public CommonResponse<List<ProductResponse>> getProducts(@Valid @ModelAttribute ProductSearchRequest request) {
        List<ProductResponse> products = productListUseCase.execute().stream()
                .map(ProductResponse::from)
                .toList();
        return CommonResponse.success(products);
    }


    /**
     *  상품 상세 조회
     */
    @GetMapping("/{productId}")
    public CommonResponse<ProductResponse> getProduct(
            @Parameter(description = "상품 ID",example = "1")
            @PathVariable Long productId) {
        ProductResponse product = ProductResponse.from(productGetUseCase.execute(productId));
        return CommonResponse.success(product);
    }

    /**
     * 상품 등록
     */
    @PostMapping
    public CommonResponse createProduct(@RequestBody @Valid ProductCreateRequest request) {
        ProductDto dto  = productCreateUseCase.execute(request.toCommand());
        return CommonResponse.success(ProductResponse.from(dto));
    }

    @GetMapping("/popular")
    public CommonResponse getPopularProduct(@RequestBody @Valid ProductPopularRequest request) {
        List<ProductResponse> products = productPopularUseCase.execute(request.toCommand()).stream()
                .map(ProductResponse::from)
                .toList();
        return CommonResponse.success(products);
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{productId}")
    public CommonResponse<ProductResponse> updateProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            @RequestBody @Valid ProductUpdateRequest request) {
        Product updatedProduct = productService.updateProduct(request.toCommand(productId));
        return CommonResponse.success(ProductResponse.from(ProductDto.from(updatedProduct)));
    }








}