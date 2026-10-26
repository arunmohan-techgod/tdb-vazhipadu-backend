package com.temple.vazhipadu_booking.order;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.temple.vazhipadu_booking.order.dto.CreateOrderRequest;
import com.temple.vazhipadu_booking.order.dto.OrderResponse;
import com.temple.vazhipadu_booking.order.entity.OrderStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Order ID must be positive");
        }
        return orderService.getById(id);
    }
    
    @GetMapping
    public Page<OrderResponse> searchOrders(
            @RequestParam(required = false) Long templeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate offeringDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return orderService.searchOrders(
            templeId,
            offeringDate,
            status,
            phoneNumber,
            page,
            size
        );
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TempleOrder> cancelOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
