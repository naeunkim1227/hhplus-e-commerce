package io.hhplus.ecommerce.cart.domain.service;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.exception.CartErrorCode;
import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import io.hhplus.ecommerce.cart.domain.validator.CartValidator;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 장바구니 도메인 서비스
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartValidator cartValidator;
    private final ProductService productService;

    /**
     * 장바구니에 상품 추가
     * - 이미 존재하면 수량 증가, 없으면 새로 추가
     */
    public CartItem addOrUpdateCartItem(CartItemAddCommand command) {
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(command.getUserId(), command.getProductId());
        CartItem newitem;

        if (existingItem.isPresent()) {
            CartItem existingCartItem = existingItem.get();
            cartValidator.validate(command.getUserId(), existingCartItem);
            newitem = CartItem.builder()
                    .id(existingCartItem.getId())
                    .userId(existingCartItem.getUserId())
                    .productId(existingCartItem.getProductId())
                    .quantity(existingCartItem.getQuantity() + command.getQuantity())
                    .createdAt(existingCartItem.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

        } else {
            newitem = CartItem.builder()
                    .userId(command.getUserId())
                    .productId(command.getProductId())
                    .quantity(command.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        return cartRepository.save(newitem);
    }

    /**
     * 장바구니 수량 변경
     */
    @Transactional
    public CartItem updateCartItem(CartItemUpdateCommand command) {
        CartItem existingItem = cartRepository.findById(command.getCartItemId())
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));

        existingItem.updateQuantity(command.getQuantity());
        return existingItem;
    }

    /**
     * 사용자의 장바구니 아이템 조회
     */
    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);
    }


    /**
     * 사용자의 장바구니 아이템 조회 (상품 정보 포함)
     * - CartItem + Product 정보를 조합하여 DTO로 반환
     * - N+1 문제 해결: JOIN으로 한 번에 조회 (총 1번 쿼리)
     */
    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItemsWithProductInfo(Long userId) {
        return cartRepository.findByUserIdWithProduct(userId).stream()
                .map(vo -> CartItemDto.builder()
                        .cartItemId(vo.getCartItemId())
                        .userId(vo.getUserId())
                        .productId(vo.getProductId())
                        .quantity(vo.getQuantity())
                        .productName(vo.getProductName())
                        .price(vo.getPrice())
                        .subtotal(vo.getPrice().multiply(java.math.BigDecimal.valueOf(vo.getQuantity())))
                        .totalStock(vo.getStock())
                        .build())
                .toList();
    }


    /**
     * 장바구니 아이템 삭제
     */
    public void deleteCartItem(Long userId, Long cartItemId) {
        Optional<CartItem> existingItem = cartRepository.findById(cartItemId);
        if(existingItem.isEmpty()){
            throw new BusinessException(CartErrorCode.CART_NOT_FOUND);
        }

        cartValidator.validate(userId, existingItem.get());
        cartRepository.deleteById(cartItemId);
    }

    /**
     * 사용자의 전체 장바구니 비우기
     */
    public void clearCart(Long userId) {
        List<CartItem> cartItemList = cartRepository.findByUserId(userId);
        if(cartItemList.isEmpty()){
            throw new BusinessException(CartErrorCode.CART_NOT_FOUND);
        }
        cartRepository.deleteByUserId(userId);
    }


    /**
     * cartItemIds으로 다건 조회
     * @param cartItemIds
     * @return
     */
    public List<CartItem> getCartItemsByIds(List<Long> cartItemIds) {
        List<CartItem> cartItems = cartRepository.findByIdIn(cartItemIds);

        // 요청한 ID와 조회된 결과 개수가 다르면 존재하지 않는 CartItem이 있음
        if (cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(CartErrorCode.CART_NOT_FOUND);
        }

        return cartItems;
    }

    /**
     * 특정 장바구니 아이템들 삭제 (주문 완료 후)
     */
    public void clearCartItems(List<Long> cartItemIds) {
        cartItemIds.forEach(cartRepository::deleteById);
    }
}