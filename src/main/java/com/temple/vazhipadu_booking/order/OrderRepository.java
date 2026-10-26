package com.temple.vazhipadu_booking.order;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.temple.vazhipadu_booking.order.entity.OrderStatus;

public interface OrderRepository
        extends JpaRepository<TempleOrder, Long>,
        JpaSpecificationExecutor<TempleOrder> {
	
	@Query("""
		    SELECT COALESCE(SUM(i.quantity), 0)
		    FROM OrderItem i
		    WHERE i.offering.id = :offeringId
		      AND i.order.offeringDate = :offeringDate
		      AND i.order.status <> :cancelledStatus
		    """)
		Long sumBookedQuantity(
		    @Param("offeringId") Long offeringId,
		    @Param("offeringDate") LocalDate offeringDate,
		    @Param("cancelledStatus") OrderStatus cancelledStatus
		);
}
