package com.temple.vazhipadu_booking.report;

import java.math.BigDecimal;

public interface OfferingCollectionProjection {
    Long getOfferingId();
    String getOfferingName();
    Long getQuantity();
    BigDecimal getAmount();
}