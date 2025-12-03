package io.hhplus.ecommerce.product.application.usecase;

import io.hhplus.ecommerce.product.domain.service.ProductService;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeUseCase {
    private final ProductService productService;
    private final RankingService rankingService;

    /**
     * 상품 좋아요 추가
     */
    public void addLike(Long productId, Long userId) {
        boolean success = productService.increaseLikeCount(productId, userId);
        if(success) rankingService.recordLike(productId);
    }

    /**
     * 상품 좋아요 취소
     */
    public void removeLike(Long productId, Long userId) {
        boolean success = productService.decreaseLikeCount(productId, userId);
        if(success) rankingService.removeLike(productId);
    }
}