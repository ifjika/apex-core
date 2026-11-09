package com.apex.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Should enforce 4 decimal scale and bankers rounding HALF_EVEN")
    void shouldEnforceScaleAndRounding() {
        Money money = Money.of("100.55555", "IDR");
        assertThat(money.amount()).isEqualTo(new BigDecimal("100.5556"));
    }

    @Test
    @DisplayName("Should accurately perform addition without floating-point error")
    void shouldPerformAdditionAccurately() {
        Money m1 = Money.of("10.1234", "IDR");
        Money m2 = Money.of("20.4321", "IDR");

        Money sum = m1.add(m2);

        assertThat(sum.amount()).isEqualTo(new BigDecimal("30.5555"));
        assertThat(sum.currency()).isEqualTo("IDR");
    }

    @Test
    @DisplayName("Should reject arithmetic across different currencies")
    void shouldRejectDifferentCurrencies() {
        Money idr = Money.of("1000", "IDR");
        Money usd = Money.of("10", "USD");

        assertThatThrownBy(() -> idr.add(usd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Should correctly evaluate relational comparisons")
    void shouldCompareCorrectly() {
        Money smaller = Money.of("50.0000", "IDR");
        Money larger = Money.of("100.0000", "IDR");

        assertThat(larger.isGreaterThan(smaller)).isTrue();
        assertThat(smaller.isLessThan(larger)).isTrue();
        assertThat(smaller.isGreaterThan(larger)).isFalse();
    }
}

