package io.hhplus.ecommerce.cart.domain.service;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.exception.CartErrorCode;
import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import io.hhplus.ecommerce.cart.domain.validator.CartValidator;
import io.hhplus.ecommerce.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public CartItem updateCartItem(CartItemUpdateCommand command) {
        Optional<CartItem> existingItem = cartRepository.findById(command.getCartItemId());
        if(existingItem.isEmpty()){
            throw new BusinessException(CartErrorCode.CART_NOT_FOUND);
        }

        CartItem cartItem = CartItem.builder()
                        .quantity(existingItem.get().getQuantity() + command.getQuantity())
                        .cartItemId(command.getCartItemId())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        return  cartRepository.save(cartItem);
    }

    /**
     * 사용자의 장바구니 아이템 조회
     */
    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    /**
     * 장바구니 아이템 삭제
     */
    public void deleteCartItem(CartItemDeleteCommand command) {
        Optional<CartItem> existingItem = cartRepository.findById(command.getCartItemId());
        if(existingItem.isEmpty()){
            throw new BusinessException(CartErrorCode.CART_NOT_FOUND);
        }

        cartValidator.validate(command.getUserId(), existingItem.get());
        cartRepository.deleteById(command.getCartItemId());
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
        List<CartItem> cartItems = cartItemIds.stream()
                .map(cartItemId -> cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND)))
                .toList();

        return cartItems;
    }

    /**
     * 특정 장바구니 아이템들 삭제 (주문 완료 후)
     */
    public void clearCartItems(List<Long> cartItemIds) {
        cartItemIds.forEach(cartRepository::deleteById);
    }
}