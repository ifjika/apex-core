package com.apex.common.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable Money Value Object ensuring exact precision and eliminating floating point drift.
 * Scale is fixed at 4 decimal places with RoundingMode.HALF_EVEN (banker's rounding).
 */
public record Money(BigDecimal amount, String currency) implements Serializable, Comparable<Money> {

    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    public static final String DEFAULT_CURRENCY = "IDR";

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        amount = amount.setScale(SCALE, ROUNDING_MODE);
        currency = currency.toUpperCase().trim();
    }

    public static Money of(String amountStr) {
        return new Money(new BigDecimal(amountStr), DEFAULT_CURRENCY);
    }

    public static Money of(String amountStr, String currency) {
        return new Money(new BigDecimal(amountStr), currency);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(long factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor must not be null");
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void validateSameCurrency(Money other) {
        Objects.requireNonNull(other, "Comparison money must not be null");
        if (!this.currency.equalsIgnoreCase(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: cannot operate on %s and %s", this.currency, other.currency)
            );
        }
    }

    @Override
    public int compareTo(Money o) {
        validateSameCurrency(o);
        return this.amount.compareTo(o.amount);
    }

    @Override
    public String toString() {
        return currency + " " + amount.toPlainString();
    }
}

