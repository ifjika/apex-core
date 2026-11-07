package com.apex.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetailResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.of(
            "https://api.apex-core.internal/errors/not-found",
            "Resource Not Found",
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getErrorCode()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DiscrepancyException.class)
    public ResponseEntity<ProblemDetailResponse> handleDiscrepancy(DiscrepancyException ex, HttpServletRequest request) {
        List<ProblemDetailResponse.FieldErrorItem> errors = ex.getDiscrepancyReasons().stream()
            .map(r -> new ProblemDetailResponse.FieldErrorItem("matching", null, r))
            .toList();

        ProblemDetailResponse response = ProblemDetailResponse.withFieldErrors(
            "https://api.apex-core.internal/errors/three-way-match-discrepancy",
            "3-Way Matching Discrepancy Detected",
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getErrorCode(),
            errors
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetailResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.of(
            "https://api.apex-core.internal/errors/business-rule-violation",
            "Business Rule Violation",
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getErrorCode()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ProblemDetailResponse.FieldErrorItem> fieldErrors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ProblemDetailResponse.FieldErrorItem(
                fe.getField(),
                fe.getRejectedValue(),
                fe.getDefaultMessage()
            ));
        }

        ProblemDetailResponse response = ProblemDetailResponse.withFieldErrors(
            "https://api.apex-core.internal/errors/invalid-payload",
            "Validation Failed",
            HttpStatus.BAD_REQUEST.value(),
            "The request payload failed validation checks.",
            request.getRequestURI(),
            "APEX_VALIDATION_ERROR",
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ProblemDetailResponse response = ProblemDetailResponse.of(
            "https://api.apex-core.internal/errors/internal-server-error",
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred while processing the request.",
            request.getRequestURI(),
            "APEX_INTERNAL_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

