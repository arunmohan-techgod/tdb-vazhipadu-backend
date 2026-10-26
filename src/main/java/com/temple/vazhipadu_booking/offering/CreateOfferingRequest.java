package com.temple.vazhipadu_booking.offering;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateOfferingRequest(
    @NotBlank(message = "Offering name is required")
    @Size(max = 100)
    String name,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Price supports up to two decimal places")
    BigDecimal price,

    @NotNull(message = "Active flag is required")
    Boolean active,
    
    @Positive(message = "Daily limit must be greater than zero")
    Integer dailyLimit
) {}

