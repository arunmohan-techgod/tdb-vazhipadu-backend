package com.temple.vazhipadu_booking.report;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.temple.vazhipadu_booking.order.OrderItemRepository;
import com.temple.vazhipadu_booking.order.entity.OrderStatus;

@Service
public class DailyCollectionService {

    private static final ZoneId IST =
            ZoneId.of("Asia/Kolkata");

    private final OrderItemRepository orderItemRepository;

    public DailyCollectionService(
            OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public DailyCollectionReport getDailyCollection(
            Long templeId, LocalDate date) {

        var start = date.atStartOfDay(IST).toInstant();
        var end = date.plusDays(1)
                .atStartOfDay(IST)
                .toInstant();

        var rows = orderItemRepository.findDailyCollection(
                templeId,
                start,
                end,
                OrderStatus.CANCELLED
        );

        List<OfferingCollectionLine> offerings = rows.stream()
                .map(row -> new OfferingCollectionLine(
                        row.getOfferingId(),
                        row.getOfferingName(),
                        row.getQuantity(),
                        row.getAmount().setScale(
                                2, RoundingMode.UNNECESSARY)
                ))
                .toList();

        BigDecimal total = offerings.stream()
                .map(OfferingCollectionLine::amount)
                .reduce(
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal::add
                );

        return new DailyCollectionReport(
                templeId, date, total, offerings
        );
    }
}
