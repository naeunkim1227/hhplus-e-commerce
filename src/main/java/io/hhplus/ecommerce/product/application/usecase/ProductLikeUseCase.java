package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.order.domain.event.OrderCompletedEvent;
import io.hhplus.ecommerce.product.domain.event.ProductLikedEvent;
import io.hhplus.ecommerce.product.domain.event.ProductUnLikedEvent;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeUseCase {
    private final ProductService productService;
    private final RankingService rankingService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 상품 좋아요 추가
     */
    public void addLike(Long productId, Long userId) {
        boolean success = productService.increaseLikeCount(productId, userId);
        if(success) eventPublisher.publishEvent(new ProductLikedEvent(productId, userId));

    }

    /**
     * 상품 좋아요 취소
     */
    public void removeLike(Long productId, Long userId) {
        boolean success = productService.decreaseLikeCount(productId, userId);
        if(success) eventPublisher.publishEvent(new ProductUnLikedEvent(productId, userId));
    }
}