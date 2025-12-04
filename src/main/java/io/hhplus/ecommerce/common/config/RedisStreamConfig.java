package io.hhplus.ecommerce.common.config;


import io.hhplus.ecommerce.common.stream.CouponIssueStreamConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

    private final RedisTemplate<String, String> streamRedisTemplate;  // Stream 전용 템플릿 사용!
    private final CouponIssueStreamConsumer consumer;

    private static final String GROUP_NAME = "coupon-issue-group";
    private static final String STREAM_KEY = "coupon:issue:stream";  // 단일 스트림

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer() {

        log.info("Redis Stream Container 초기화");

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .errorHandler(throwable -> log.error("Stream error", throwable))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(streamRedisTemplate.getConnectionFactory(), options);

        // Consumer Group 생성 (스트림 없으면 자동 생성)
        try {
            // 스트림이 없으면 빈 메시지로 생성
            Long size = streamRedisTemplate.opsForStream().size(STREAM_KEY);
            if (size == null) {
                streamRedisTemplate.opsForStream().add(STREAM_KEY, java.util.Map.of("init", "true"));
            }
        } catch (Exception e) {
            // 스트림 생성 시도
            streamRedisTemplate.opsForStream().add(STREAM_KEY, java.util.Map.of("init", "true"));
        }

        try {
            streamRedisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.from("0"), GROUP_NAME);
        } catch (Exception e) {
        }

        // 리스너 등록
        container.receive(
                Consumer.from(GROUP_NAME, "consumer-1"),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                consumer
        );

        container.start();
        return container;
    }



}
