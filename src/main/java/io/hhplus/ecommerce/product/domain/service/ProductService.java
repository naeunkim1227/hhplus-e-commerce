package io.hhplus.ecommerce.product.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.entity.ProductReservation;
import io.hhplus.ecommerce.product.domain.entity.ProductReservationStatus;
import io.hhplus.ecommerce.product.domain.exception.ProductErrorCode;
import io.hhplus.ecommerce.product.domain.repository.ProductRepository;
import io.hhplus.ecommerce.product.domain.repository.ProductReservationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductReservationRepository reservationRepository;
    private final ProductReservationRepository productReservationRepository;

    /**
     * 상품 ID로 상품 조회
     */
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
    public void validate(Product product, int quantity){
        Long reservedQuantity = productReservationRepository.getTotalReservedQuantity(product.getId());
        if((reservedQuantity + product.getStock()) - quantity <  0){
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 재고 선점 가능 여부 확인 (장바구니 담기용)
     */
    public boolean isStockAvailableForReservation(Long productId, int quantity) {
        Product product = getProduct(productId);
        Long reservedQuantity = productReservationRepository.getTotalReservedQuantity(productId);
        long availableStock = product.getStock() - reservedQuantity;
        return availableStock >= quantity;
    }

    /**
     * 모든 상품 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 상품 생성
     */
    public Product createProduct(@Valid ProductCreateCommand command) {
        Product product =  Product.create(command);
        return productRepository.save(product);
    }

    /**
     * 주문선점
     */
    public Product reserveStock(Long orderId, Long productId, int quantity) {
        Product product = getProduct(productId);
        ProductReservation.create(orderId, productId, quantity);
        validate(product, quantity);
        return productRepository.save(product);
    }
    /**
     * 재고 선점 확정 (주문 완료 시)
     * @param orderId 주문 ID
     */
    public void confirmReservation(Long orderId) {
        ProductReservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.RESERVATION_NOT_FOUND));

        reservation.changeStatus(ProductReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }

    /**
     * 재고 선점 해제 (주문 취소 시)
     * @param orderId 주문 ID
     */
    public void releaseReservation(Long orderId) {
        ProductReservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.RESERVATION_NOT_FOUND));
        reservation.changeStatus(ProductReservationStatus.RELEASED);
        reservationRepository.save(reservation);
    }

    /**
     * 재고 차감 (결제 완료 후)
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     */
    public void decreaseStock(Long productId, int quantity) {
        Product product = getProduct(productId);
        product.decreaseStock(quantity);
        productRepository.save(product);
    }

}