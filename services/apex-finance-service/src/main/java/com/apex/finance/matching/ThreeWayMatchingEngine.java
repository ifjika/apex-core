package com.apex.finance.matching;

import com.apex.finance.domain.GoodsReceipt;
import com.apex.finance.domain.GRLineItem;
import com.apex.finance.domain.VendorInvoice;
import com.apex.finance.domain.VILineItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Automated 3-Way Matching Engine implementing mathematical invariants across PO, GR, and VI data.
 */
@Component
public class ThreeWayMatchingEngine {

    private final BigDecimal toleranceRate;

    public ThreeWayMatchingEngine(
        @Value("${apex.matching.tolerance-percentage:0.005}") double toleranceRate
    ) {
        this.toleranceRate = BigDecimal.valueOf(toleranceRate);
    }

    public record MatchResult(
        MatchStatus status,
        boolean hasDiscrepancy,
        BigDecimal varianceAmount,
        List<String> discrepancyReasons
    ) {
        public enum MatchStatus {
            MATCHED,
            MATCHED_WITH_TOLERANCE,
            DISCREPANCY
        }
    }

    /**
     * Reconciles line items between Goods Receipt and Vendor Invoice.
     * Note: In a production microservice environment, PO snapshot line items
     * are either projected locally from PO_ISSUED Kafka events or queried via IPC.
     */
    public MatchResult evaluate(
        GoodsReceipt goodsReceipt,
        VendorInvoice vendorInvoice,
        BigDecimal expectedPoTotalAmount
    ) {
        List<String> mismatches = new ArrayList<>();

        if (!vendorInvoice.getPurchaseOrderId().equalsIgnoreCase(goodsReceipt.getPurchaseOrderId())) {
            mismatches.add(String.format(
                "Mismatched PO Reference: Invoice cites [%s], GR cites [%s]",
                vendorInvoice.getPurchaseOrderId(), goodsReceipt.getPurchaseOrderId()
            ));
            return new MatchResult(MatchResult.MatchStatus.DISCREPANCY, true, BigDecimal.ZERO, mismatches);
        }

        Map<String, Integer> grQuantities = goodsReceipt.getLineItems().stream()
            .collect(Collectors.toMap(GRLineItem::getPoLineItemId, GRLineItem::getReceivedQuantity));

        for (VILineItem viItem : vendorInvoice.getLineItems()) {
            Integer receivedQty = grQuantities.get(viItem.getPoLineItemId());

            if (receivedQty == null) {
                mismatches.add("Billed item not found in warehouse Goods Receipt: " + viItem.getPoLineItemId());
                continue;
            }

            // Quantity Invariant: Invoiced quantity cannot exceed received warehouse quantity
            if (viItem.getInvoicedQuantity() > receivedQty) {
                mismatches.add(String.format(
                    "Quantity discrepancy for item [%s]: Invoiced (%d) > Received (%d)",
                    viItem.getPoLineItemId(), viItem.getInvoicedQuantity(), receivedQty
                ));
            }
        }

        // Monetary Invariant: Total invoice amount variance check
        BigDecimal expectedTotalWithTax = expectedPoTotalAmount.add(vendorInvoice.getTaxAmount());
        BigDecimal variance = vendorInvoice.getTotalAmount().subtract(expectedTotalWithTax).abs();
        BigDecimal maxAllowedVariance = expectedTotalWithTax.multiply(this.toleranceRate).setScale(4, RoundingMode.HALF_EVEN);

        if (!mismatches.isEmpty()) {
            return new MatchResult(MatchResult.MatchStatus.DISCREPANCY, true, variance, mismatches);
        }

        if (variance.compareTo(BigDecimal.ZERO) == 0) {
            return new MatchResult(MatchResult.MatchStatus.MATCHED, false, BigDecimal.ZERO, List.of());
        }

        if (variance.compareTo(maxAllowedVariance) <= 0) {
            return new MatchResult(MatchResult.MatchStatus.MATCHED_WITH_TOLERANCE, false, variance, List.of(
                "Variance " + variance + " IDR is within permitted tolerance threshold of " + maxAllowedVariance
            ));
        }

        mismatches.add(String.format(
            "Monetary variance [%s] exceeds permitted tolerance limit [%s]",
            variance, maxAllowedVariance
        ));
        return new MatchResult(MatchResult.MatchStatus.DISCREPANCY, true, variance, mismatches);
    }
}

