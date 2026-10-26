package com.temple.vazhipadu_booking.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Devotee name is required")
    @Size(max = 100)
    String devoteeName,

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[+0-9][0-9\\s-]{6,19}$",
        message = "Phone number format is invalid"
    )
    String phoneNumber,

    @NotBlank(message = "Birth star is required")
    @Size(max = 50)
    String nakshatra,

    @NotNull(message = "Offering date is required")
    LocalDate offeringDate,

    @NotEmpty(message = "At least one offering is required")
    List<@Valid OrderItemRequest> items
) {
    public record OrderItemRequest(
        @NotNull(message = "Offering ID is required")
        @Positive(message = "Offering ID must be positive")
        Long offeringId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity
    ) {}
}

