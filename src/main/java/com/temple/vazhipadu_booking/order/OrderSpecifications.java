package com.temple.vazhipadu_booking.order;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import com.temple.vazhipadu_booking.order.entity.OrderStatus;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<TempleOrder> search(
            Long templeId,
            LocalDate offeringDate,
            OrderStatus status,
            String phoneNumber) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (templeId != null) {
                predicates = cb.and(
                    predicates,
                    cb.equal(root.get("templeId"), templeId)
                );
            }

            if (offeringDate != null) {
                predicates = cb.and(
                    predicates,
                    cb.equal(root.get("offeringDate"), offeringDate)
                );
            }

            if (status != null) {
                predicates = cb.and(
                    predicates,
                    cb.equal(root.get("status"), status)
                );
            }

            if (phoneNumber != null && !phoneNumber.isBlank()) {
                predicates = cb.and(
                    predicates,
                    cb.equal(root.get("phoneNumber"), phoneNumber.trim())
                );
            }

            return predicates;
        };
    }
}