package com.temple.vazhipadu_booking.order;

	
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.temple.vazhipadu_booking.common.ResourceNotFoundException;
import com.temple.vazhipadu_booking.offering.Offering;
import com.temple.vazhipadu_booking.offering.OfferingRepository;
import com.temple.vazhipadu_booking.order.dto.CreateOrderRequest;
import com.temple.vazhipadu_booking.order.dto.OrderResponse;
import com.temple.vazhipadu_booking.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
        BigDecimal total = BigDecimal.ZERO;

        // Validate every offering and calculate the total on the server.
        for (var item : request.items()) {
            Offering offering = offeringRepository
                .findById(item.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Offering not found: " + item.offeringId()));

            if (!offering.isActive()) {
                throw new IllegalArgumentException(
                    "Offering is inactive: " + offering.getName());
            }

            total = total.add(
                offering.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantity())));
        }

        TempleOrder order = new TempleOrder(
            request.devoteeName().trim(),
            request.phoneNumber().trim(),
            request.nakshatra().trim(),
            request.offeringDate(),
            total
        );

        // Build order items using database prices, not client prices.
        for (var item : request.items()) {
            Offering offering = offeringRepository
                .findById(item.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Offering not found: " + item.offeringId()));

            order.addItem(new OrderItem(
                offering, item.quantity(), offering.getPrice()));
        }

        TempleOrder saved = orderRepository.save(order);
        return toResponse(saved);
    }

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

