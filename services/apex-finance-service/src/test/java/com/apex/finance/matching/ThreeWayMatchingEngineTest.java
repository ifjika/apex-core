package com.apex.finance.matching;

import com.apex.finance.domain.GoodsReceipt;
import com.apex.finance.domain.GRLineItem;
import com.apex.finance.domain.VendorInvoice;
import com.apex.finance.domain.VILineItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ThreeWayMatchingEngineTest {

    private ThreeWayMatchingEngine engine;

    @BeforeEach
    void setUp() {
        // Configure 0.5% (0.005) tolerance
        engine = new ThreeWayMatchingEngine(0.005);
    }

    @Test
    @DisplayName("Should return MATCHED when PO, GR, and VI quantities and prices align perfectly")
    void shouldReturnMatchedWhenQuantitiesAndPricesAlign() {
        GoodsReceipt gr = new GoodsReceipt("gr-1", "GR-001", "PO-100", "emp-receiver");
        gr.addLineItem(new GRLineItem("item-1", 10));

        VendorInvoice vi = new VendorInvoice("vi-1", "INV-001", "vend-1", "PO-100", "gr-1", BigDecimal.ZERO);
        vi.addLineItem(new VILineItem("item-1", 10, new BigDecimal("100000.00")));

        BigDecimal expectedPoAmount = new BigDecimal("1000000.00"); // 10 * 100,000

        ThreeWayMatchingEngine.MatchResult result = engine.evaluate(gr, vi, expectedPoAmount);

        assertThat(result.status()).isEqualTo(ThreeWayMatchingEngine.MatchResult.MatchStatus.MATCHED);
        assertThat(result.hasDiscrepancy()).isFalse();
        assertThat(result.varianceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return DISCREPANCY when invoiced quantity exceeds received quantity")
    void shouldReturnDiscrepancyWhenInvoicedQtyExceedsReceivedQty() {
        GoodsReceipt gr = new GoodsReceipt("gr-2", "GR-002", "PO-200", "emp-receiver");
        gr.addLineItem(new GRLineItem("item-1", 8)); // Received 8

        VendorInvoice vi = new VendorInvoice("vi-2", "INV-002", "vend-1", "PO-200", "gr-2", BigDecimal.ZERO);
        vi.addLineItem(new VILineItem("item-1", 10, new BigDecimal("100000.00"))); // Billed 10

        BigDecimal expectedPoAmount = new BigDecimal("1000000.00");

        ThreeWayMatchingEngine.MatchResult result = engine.evaluate(gr, vi, expectedPoAmount);

        assertThat(result.status()).isEqualTo(ThreeWayMatchingEngine.MatchResult.MatchStatus.DISCREPANCY);
        assertThat(result.hasDiscrepancy()).isTrue();
        assertThat(result.discrepancyReasons()).anyMatch(reason -> reason.contains("Invoiced (10) > Received (8)"));
    }

    @Test
    @DisplayName("Should return MATCHED_WITH_TOLERANCE when variance is within 0.5% limit")
    void shouldReturnMatchedWithToleranceWithinThreshold() {
        GoodsReceipt gr = new GoodsReceipt("gr-3", "GR-003", "PO-300", "emp-receiver");
        gr.addLineItem(new GRLineItem("item-1", 10));

        VendorInvoice vi = new VendorInvoice("vi-3", "INV-003", "vend-1", "PO-300", "gr-3", BigDecimal.ZERO);
        // Unit price slightly varied causing a 3,000 IDR delta on a 1,000,000 IDR order (0.3% variance <= 0.5% allowed)
        vi.addLineItem(new VILineItem("item-1", 10, new BigDecimal("100300.00")));

        BigDecimal expectedPoAmount = new BigDecimal("1000000.00");

        ThreeWayMatchingEngine.MatchResult result = engine.evaluate(gr, vi, expectedPoAmount);

        assertThat(result.status()).isEqualTo(ThreeWayMatchingEngine.MatchResult.MatchStatus.MATCHED_WITH_TOLERANCE);
        assertThat(result.hasDiscrepancy()).isFalse();
    }
}

