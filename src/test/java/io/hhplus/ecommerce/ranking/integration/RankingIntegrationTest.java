package io.hhplus.ecommerce.ranking.integration;

import io.hhplus.ecommerce.common.config.redis.RedisKeyType;
import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import io.hhplus.ecommerce.product.application.usecase.ProductCreateUseCase;
import io.hhplus.ecommerce.product.application.usecase.ProductLikeUseCase;
import io.hhplus.ecommerce.ranking.domain.service.RankingService;
import io.hhplus.ecommerce.ranking.policy.RankingPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("RediRanking 통합 테스트 - RankingService + Redis")
class RankingIntegrationTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ProductCreateUseCase productCreateUseCase;

    @Autowired
    private ProductLikeUseCase productLikeUseCase;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Test
    @DisplayName("상품 좋아요 기록 시 랭킹 점수가 증가한다")
    void recordLike_IncreasesRankingScore() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
//        Long productId = 771L;
//        Long userId = 1L;

        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double originScore = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        // When
        rankingService.recordLike(productId);

        // Then
        Double addScore = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        Assertions.assertAll(
                "좋아요 기록 후 랭킹 점수 검증",
                () -> assertThat(addScore).isEqualTo(originScore + RankingPolicy.LIKE_INCREMENT_SCORE)
        );
    }

    @Test
    @DisplayName("상품 좋아요 취소 시 랭킹 점수가 감소한다")
    void removeLike_DecreasesRankingScore() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        rankingService.recordLike(productId);

        // When
        rankingService.removeLike(productId);

        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double score = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        Assertions.assertAll(
                "좋아요 취소 후 랭킹 점수 검증",
                () -> assertThat(score).isNotNull(),
                () -> assertThat(score).isEqualTo(0.0)
        );
    }

    @Test
    @DisplayName("상품 조회 기록 시 랭킹 점수가 증가한다")
    void recordView_IncreasesRankingScore() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        // When
        rankingService.recordView(productId, userId);


        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double score = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        Assertions.assertAll(
                "조회 기록 후 랭킹 점수 검증",
                () -> assertThat(score).isNotNull(),
                () -> assertThat(score).isEqualTo(RankingPolicy.VIEW_INCREMENT_SCORE)
        );
    }

    @Test
    @DisplayName("동일 사용자가 같은 상품을 여러 번 조회해도 랭킹 점수는 한 번만 증가한다")
    void recordView_SameUserMultipleTimes_ScoreIncreasesOnce() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        // When: 같은 사용자가 3번 조회
        rankingService.recordView(productId, userId);
        rankingService.recordView(productId, userId);
        rankingService.recordView(productId, userId);

        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double score = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        Assertions.assertAll(
                "중복 조회 시 랭킹 점수 검증",
                () -> assertThat(score).isNotNull(),
                () -> assertThat(score).isEqualTo(RankingPolicy.VIEW_INCREMENT_SCORE),
                () -> System.out.println("조회 기록 점수: " + score)
        );
    }

    @Test
    @DisplayName("상품 주문 기록 시 랭킹 점수가 증가한다")
    void recordOrder_IncreasesRankingScore() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        // When
        rankingService.recordOrder(productId);

        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double score = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        Assertions.assertAll(
                "주문 기록 후 랭킹 점수 검증",
                () -> assertThat(score).isNotNull(),
                () -> assertThat(score).isEqualTo(RankingPolicy.ORDER_INCREMENT_SCORE)
        );
    }

    @Test
    @DisplayName("상품 장바구니 추가 시 랭킹 점수가 증가한다")
    void recordCart_IncreasesRankingScore() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        // When
        rankingService.recordCart(productId, userId);

        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double score = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        Assertions.assertAll(
                "장바구니 추가 후 랭킹 점수 검증",
                () -> assertThat(score).isNotNull(),
                () -> assertThat(score).isEqualTo(RankingPolicy.CART_INCREMENT_SCORE)
        );
    }

    @Test
    @DisplayName("조회 + 좋아요 + 장바구니 + 주문 통합 진행")
    void multipleActions_AccumulateRankingScore() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        // When: 조회, 좋아요, 장바구니, 주문
        rankingService.recordView(productId, userId);
        rankingService.recordLike(productId);
        rankingService.recordCart(productId, userId);
        rankingService.recordOrder(productId);

        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double score = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        double expectedScore = RankingPolicy.VIEW_INCREMENT_SCORE
                + RankingPolicy.LIKE_INCREMENT_SCORE
                + RankingPolicy.CART_INCREMENT_SCORE
                + RankingPolicy.ORDER_INCREMENT_SCORE;

        Assertions.assertAll(
                "여러 액션 후 랭킹 점수 누적 검증",
                () -> assertThat(score).isNotNull(),
                () -> assertThat(score).isEqualTo(expectedScore),
                () -> System.out.println("누적 점수: " + score + " (예상: " + expectedScore + ")")
        );
    }

    @Test
    @DisplayName("랭킹 데이터를 점수 순으로 추출할 수 있다")
    void extractRankingData_OrderedByScore() {
        // Given: 3개 상품 생성 및 다른 점수 부여
        ProductDto product1 = createProduct("상품1", 10000, 100L);
        ProductDto product2 = createProduct("상품2", 20000, 100L);
        ProductDto product3 = createProduct("상품3", 30000, 100L);

        // 상품1: 조회만 (1점)
        rankingService.recordView(product1.getId(), 1L);

        // 상품2: 조회 + 좋아요
        rankingService.recordView(product2.getId(), 2L);
        rankingService.recordLike(product2.getId());

        // 상품3: 조회 + 좋아요 + 주문 (1 + 5 + 10 = 16점)
        rankingService.recordView(product3.getId(), 3L);
        rankingService.recordLike(product3.getId());
        rankingService.recordOrder(product3.getId());

        // When: 랭킹 데이터 추출 (내림차순)
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Set<ZSetOperations.TypedTuple<String>> ranking = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, 0, -1);

        // Then
        assertThat(ranking).hasSize(3);

        // 순위 검증
        ZSetOperations.TypedTuple<String>[] rankingArray = ranking.toArray(new ZSetOperations.TypedTuple[0]);

        Assertions.assertAll(
                "랭킹 순위 검증",
                () -> assertThat(rankingArray[0].getValue()).isEqualTo(String.valueOf(product3.getId())),
                () -> assertThat(rankingArray[1].getValue()).isEqualTo(String.valueOf(product2.getId())),
                () -> assertThat(rankingArray[2].getValue()).isEqualTo(String.valueOf(product1.getId()))
        );
    }

    @Test
    @DisplayName("ProductLikeUseCase를 통한 좋아요 추가 시 랭킹 점수와 좋아요 수가 모두 증가한다")
    void productLikeUseCase_IncreasesRankingAndLikeCount() {
        // Given
        ProductDto product = createProduct("테스트 상품", 10000, 100L);
        Long productId = product.getId();
        Long userId = 1L;

        // When
        productLikeUseCase.addLike(productId, userId);

        // Then
        String rankingKey = RedisKeyType.RANKING_POPULAR.getKey();
        Double rankingScore = redisTemplate.opsForZSet().score(rankingKey, String.valueOf(productId));

        String likeCountKey = RedisKeyType.PRODUCT_LIKE.getKey(productId);
        String likeCount = redisTemplate.opsForValue().get(likeCountKey);


        Assertions.assertAll(
                "ProductLikeUseCase를 통한 좋아요 추가 검증",
                () -> assertThat(rankingScore).isNotNull(),
                () -> assertThat(rankingScore).isEqualTo(RankingPolicy.LIKE_INCREMENT_SCORE),
                () -> assertThat(likeCount).isNotNull(),
                () -> assertThat(likeCount).isEqualTo("1")
        );
    }

    private ProductDto createProduct(String name, int price, Long stock) {
        ProductCreateCommand command = ProductCreateCommand.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stock(stock)
                .build();
        return productCreateUseCase.execute(command);
    }
}