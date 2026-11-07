package com.apex.common.exception;

import java.util.Collections;
import java.util.List;

public class DiscrepancyException extends BusinessException {

    private final List<String> discrepancyReasons;

    public DiscrepancyException(String message, List<String> discrepancyReasons) {
        super(message, "APEX_3WAY_MATCH_DISCREPANCY");
        this.discrepancyReasons = discrepancyReasons != null ? List.copyOf(discrepancyReasons) : Collections.emptyList();
    }

    public List<String> getDiscrepancyReasons() {
        return discrepancyReasons;
    }
}

