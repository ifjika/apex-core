package com.apex.procurement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record RequisitionCreateRequest(
    @NotBlank(message = "Cost center ID is mandatory")
    String costCenterId,

    @NotBlank(message = "Requester employee ID is mandatory")
    String requesterId,

    String currency,

    @NotEmpty(message = "At least one line item is required")
    @Valid
    List<LineItemRequest> lineItems
) implements Serializable {

    public record LineItemRequest(
        @NotBlank(message = "Item name is mandatory")
        String itemName,

        String description,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @DecimalMin(value = "0.0001", message = "Unit price must be strictly positive")
        BigDecimal unitPrice
    ) implements Serializable {}
}

