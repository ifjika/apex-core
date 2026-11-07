package com.apex.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Standard RFC 7807 Problem Details representation for APEX-Core.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetailResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    String code,
    Instant timestamp,
    List<FieldErrorItem> fieldErrors
) implements Serializable {

    public record FieldErrorItem(
        String field,
        Object rejectedValue,
        String message
    ) implements Serializable {}

    public static ProblemDetailResponse of(String type, String title, int status, String detail, String instance, String code) {
        return new ProblemDetailResponse(type, title, status, detail, instance, code, Instant.now(), null);
    }

    public static ProblemDetailResponse withFieldErrors(String type, String title, int status, String detail, String instance, String code, List<FieldErrorItem> fieldErrors) {
        return new ProblemDetailResponse(type, title, status, detail, instance, code, Instant.now(), fieldErrors);
    }
}

