package io.hhplus.ecommerce.cart.domain.repository;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.vo.CartItemWithProduct;

import java.util.List;
import java.util.Optional;

public interface CartRepository {
    CartItem save(CartItem cartItem);
    Optional<CartItem> findById(Long id);
    List<CartItem> findByIdIn(List<Long> ids);
    List<CartItem> findByUserId(Long userId);
    List<CartItemWithProduct> findByUserIdWithProduct(Long userId);
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    void deleteById(Long id);
    void deleteByUserId(Long userId);
}