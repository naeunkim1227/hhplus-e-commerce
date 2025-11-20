package io.hhplus.ecommerce.product.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.application.dto.command.ProductPopularCommand;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 ID로 상품 조회
     */
    @Transactional
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 여러 상품 ID로 상품 일괄 조회 (N+1 방지)
     */
    public List<Product> getProductsByIds(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    /**
     * 재고 확인
     */
    public void validate(Product product, int quantity){
        if(product.getStock() < quantity){
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }


    /**
     * 모든 상품 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    /**
     * 최근 N일 동안 가장 판매량이 많은 상품 조회
     */
    public List<Product> getPopularProducts(@Valid ProductPopularCommand command) {
        LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(command.getDays());
        return productRepository.findPopularProducts(startDate, command.getLimit());
    }


    /**
     * 상품 생성
     */
    public Product createProduct(@Valid ProductCreateCommand command) {
        Product product =  Product.create(command);
        return productRepository.save(product);
    }

    /**
     * 재고 차감 (조건부 UPDATE로 동시성 제어)
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        int updated = productRepository.decreaseStock(productId, quantity);
        if (updated == 0) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 재고 증가 (결제 실패 시 복구용)
     * @param productId 상품 ID
     * @param quantity 증가할 수량
     */
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        productRepository.increaseStock(productId, quantity);
    }

}