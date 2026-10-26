package com.temple.vazhipadu_booking.report;

import java.math.BigDecimal;

public record OfferingCollectionLine(
        Long offeringId,
        String offeringName,
        Long quantity,
        BigDecimal amount
) {}