package com.temple.vazhipadu_booking.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository
        extends JpaRepository<TempleOrder, Long>,
        JpaSpecificationExecutor<TempleOrder> {
}
