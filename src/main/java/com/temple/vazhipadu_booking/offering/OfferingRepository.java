package com.temple.vazhipadu_booking.offering;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferingRepository
        extends JpaRepository<Offering, Long> {
    boolean existsByNameIgnoreCase(String name);
}
