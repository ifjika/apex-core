package com.apex.common.idempotency;

import com.apex.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
public class IdempotencyAspect {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(idempotentAnnotation)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotentAnnotation) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader(idempotentAnnotation.headerName());

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(
                "Missing required idempotency key header: " + idempotentAnnotation.headerName(),
                "APEX_MISSING_IDEMPOTENCY_KEY"
            );
        }

        String redisKey = "idempotency:" + idempotencyKey;
        // Atomic SETNX (set if not exists)
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
            redisKey,
            "IN_PROGRESS",
            Duration.ofSeconds(idempotentAnnotation.ttlSeconds())
        );

        if (Boolean.FALSE.equals(acquired)) {
            throw new BusinessException(
                "Duplicate operation detected for idempotency key: " + idempotencyKey,
                "APEX_DUPLICATE_IDEMPOTENT_REQUEST"
            );
        }

        try {
            Object result = joinPoint.proceed();
            redisTemplate.opsForValue().set(redisKey, "COMPLETED", Duration.ofSeconds(idempotentAnnotation.ttlSeconds()));
            return result;
        } catch (Throwable t) {
            // Remove key if transaction failed so client may retry safely
            redisTemplate.delete(redisKey);
            throw t;
        }
    }
}

