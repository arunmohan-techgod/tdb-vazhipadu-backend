package com.temple.vazhipadu_booking.order;

	
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.temple.vazhipadu_booking.common.ResourceNotFoundException;
import com.temple.vazhipadu_booking.offering.Offering;
import com.temple.vazhipadu_booking.offering.OfferingRepository;
import com.temple.vazhipadu_booking.order.dto.CreateOrderRequest;
import com.temple.vazhipadu_booking.order.dto.OrderResponse;
import com.temple.vazhipadu_booking.order.entity.OrderStatus;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OfferingRepository offeringRepository;

    public OrderService(OrderRepository orderRepository,
                        OfferingRepository offeringRepository) {
        this.orderRepository = orderRepository;
        this.offeringRepository = offeringRepository;
    }
    
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        // Aggregate quantities in case an offering appears more than once.
        Map<Long, Integer> requestedQuantities = new TreeMap<>();

        for (var item : request.items()) {
            if (item.quantity() < 1) {
                throw new IllegalArgumentException(
                    "Quantity must be positive"
                );
            }

            requestedQuantities.merge(
                item.offeringId(), item.quantity(), Integer::sum
            );
        }

        // Lock each offering in a consistent order.
        Map<Long, Offering> offerings = new HashMap<>();

        for (Long offeringId : requestedQuantities.keySet()) {
            Offering offering = offeringRepository
                .findByIdForUpdate(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Offering not found: " + offeringId
                ));

            if (!offering.isActive()) {
                throw new IllegalArgumentException(
                    "Offering is inactive: " + offering.getName()
                );
            }

            offerings.put(offeringId, offering);
        }

        // Check daily limits while holding the offering locks.
        for (var entry : requestedQuantities.entrySet()) {
            Offering offering = offerings.get(entry.getKey());
            Integer limit = offering.getDailyLimit();

            if (limit == null) {
                continue; // Unlimited offering
            }

            long booked = orderRepository.sumBookedQuantity(
                offering.getId(),
                request.offeringDate(),
                OrderStatus.CANCELLED
            );

            if (booked + entry.getValue() > limit) {
                throw new IllegalArgumentException(
                    "Daily limit exceeded for offering: "
                        + offering.getName()
                );
            }
        }

        // Build the order only after all capacity checks pass.
        BigDecimal total = BigDecimal.ZERO;

        TempleOrder order = new TempleOrder(
            request.devoteeName().trim(),
            request.phoneNumber().trim(),
            request.nakshatra().trim(),
            request.offeringDate(),
            total
        );

        for (var item : request.items()) {
            Offering offering = offerings.get(item.offeringId());

            total = total.add(
                offering.getPrice().multiply(
                    BigDecimal.valueOf(item.quantity())
                )
            );

            order.addItem(new OrderItem(
                offering, item.quantity(), offering.getPrice()
            ));
        }

        // Ensure the order total matches its items.
        order.setTotal(total);

        TempleOrder saved = orderRepository.save(order);
        return toResponse(saved);
    }

//    @Transactional
//    public OrderResponse create(CreateOrderRequest request) {
//        BigDecimal total = BigDecimal.ZERO;
//
//        // Validate every offering and calculate the total on the server.
//        for (var item : request.items()) {
//            Offering offering = offeringRepository
//                .findById(item.offeringId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                    "Offering not found: " + item.offeringId()));
//
//            if (!offering.isActive()) {
//                throw new IllegalArgumentException(
//                    "Offering is inactive: " + offering.getName());
//            }
//
//            total = total.add(
//                offering.getPrice()
//                    .multiply(BigDecimal.valueOf(item.quantity())));
//        }
//
//        TempleOrder order = new TempleOrder(
//            request.devoteeName().trim(),
//            request.phoneNumber().trim(),
//            request.nakshatra().trim(),
//            request.offeringDate(),
//            total
//        );
//
//        // Build order items using database prices, not client prices.
//        for (var item : request.items()) {
//            Offering offering = offeringRepository
//                .findById(item.offeringId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                    "Offering not found: " + item.offeringId()));
//
//            order.addItem(new OrderItem(
//                offering, item.quantity(), offering.getPrice()));
//        }
//
//        TempleOrder saved = orderRepository.save(order);
//        return toResponse(saved);
//    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        TempleOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Order not found: " + id));

        return toResponse(order);
    }

    private OrderResponse toResponse(TempleOrder order) {
        List<OrderResponse.ItemResponse> items =
            order.getItems().stream()
                .map(item -> new OrderResponse.ItemResponse(
                    item.getOffering().getId(),
                    item.getOffering().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getUnitPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
            order.getId(),
            order.getDevoteeName(),
            order.getPhoneNumber(),
            order.getNakshatra(),
            order.getOfferingDate(),
            items,
            order.getTotal()
        );
    }
    
    public Page<OrderResponse> searchOrders(
            Long templeId,
            LocalDate offeringDate,
            OrderStatus status,
            String phoneNumber,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                "Page size must be between 1 and 100"
            );
        }

        Specification<TempleOrder> specification =
            OrderSpecifications.search(
                templeId, offeringDate, status, phoneNumber
            );

        var pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.ASC, "id")
        );

        return orderRepository.findAll(specification, pageable)
            .map(this::toResponse);
    }
}

