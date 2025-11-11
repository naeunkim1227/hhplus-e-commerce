package io.hhplus.ecommerce.cart.infrastructure.adaptor;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import io.hhplus.ecommerce.cart.infrastructure.jpa.JpaCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final JpaCartRepository jpaCartRepository;

    @Override
    public CartItem save(CartItem cartItem) {
        return jpaCartRepository.save(cartItem);
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return jpaCartRepository.findById(id);
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return jpaCartRepository.findByUserId(userId);
    }

    @Override
    public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
        return jpaCartRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public void deleteById(Long id) {
        jpaCartRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaCartRepository.deleteByUserId(userId);
    }
}