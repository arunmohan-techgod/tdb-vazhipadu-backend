package com.temple.vazhipadu_booking.order;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.temple.vazhipadu_booking.order.entity.OrderStatus;
import com.temple.vazhipadu_booking.report.OfferingCollectionProjection;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT
            oi.offering.id AS offeringId,
            oi.offering.name AS offeringName,
            SUM(oi.quantity) AS quantity,
            SUM(oi.unitPrice * oi.quantity) AS amount
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.templeId = :templeId
          AND o.createdAt >= :start
          AND o.createdAt < :end
          AND o.status <> :cancelledStatus
        GROUP BY oi.offering.id, oi.offering.name
        ORDER BY oi.offering.name
        """)
    List<OfferingCollectionProjection> findDailyCollection(
            @Param("templeId") Long templeId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("cancelledStatus") OrderStatus cancelledStatus);
}