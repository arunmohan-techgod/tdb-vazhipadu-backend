package com.temple.vazhipadu_booking.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
    Long id,
    String devoteeName,
    String phoneNumber,
    String nakshatra,
    LocalDate offeringDate,
    List<ItemResponse> items,
    BigDecimal total
) {
    public record ItemResponse(
        Long offeringId,
        String offeringName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
    ) {}
}

