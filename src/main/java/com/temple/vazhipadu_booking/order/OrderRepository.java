package com.temple.vazhipadu_booking.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository
        extends JpaRepository<TempleOrder, Long> {
}
