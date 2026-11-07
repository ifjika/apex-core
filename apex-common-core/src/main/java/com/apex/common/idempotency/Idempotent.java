package com.apex.common.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller endpoint as requiring an idempotent key header (X-Idempotency-Key).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * Header key name. Defaults to "X-Idempotency-Key".
     */
    String headerName() default "X-Idempotency-Key";

    /**
     * Key Time-To-Live in seconds in Redis. Defaults to 86400 (24 hours).
     */
    long ttlSeconds() default 86400;
}

