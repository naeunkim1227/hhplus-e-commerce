package io.hhplus.ecommerce.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        String lockKey = parseLockKey(joinPoint, distributedLock.key());
        RLock lock = redissonClient.getLock(lockKey);

        String threadName = Thread.currentThread().getName();
        log.info("[{}] 락 획득 시도: {}", threadName, lockKey);

        try {
            boolean available = lock.tryLock(
                    distributedLock.waitTime(),
                    (long) distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!available) {
                throw new IllegalStateException("락 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }

            Object result = joinPoint.proceed();
            return result;

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    private String parseLockKey(ProceedingJoinPoint joinPoint, String key) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, String.class);
    }
}

