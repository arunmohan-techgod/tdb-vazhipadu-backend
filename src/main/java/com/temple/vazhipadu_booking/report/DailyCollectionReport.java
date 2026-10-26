package com.temple.vazhipadu_booking.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyCollectionReport(
        Long templeId,
        LocalDate date,
        BigDecimal totalAmount,
        List<OfferingCollectionLine> offerings
) {}
