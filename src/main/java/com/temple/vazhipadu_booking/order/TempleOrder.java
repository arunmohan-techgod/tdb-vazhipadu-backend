package com.temple.vazhipadu_booking.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.temple.vazhipadu_booking.order.entity.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "temple_orders")
public class TempleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String devoteeName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String nakshatra;

    @Column(nullable = false)
    private LocalDate offeringDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(name = "temple_id", nullable = true)
    private Long templeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    protected TempleOrder() {
    }

    public TempleOrder(String devoteeName, String phoneNumber,
                       String nakshatra, LocalDate offeringDate,
                       BigDecimal total) {
        this.devoteeName = devoteeName;
        this.phoneNumber = phoneNumber;
        this.nakshatra = nakshatra;
        this.offeringDate = offeringDate;
        this.total = total;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() { return id; }
    public String getDevoteeName() { return devoteeName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getNakshatra() { return nakshatra; }
    public LocalDate getOfferingDate() { return offeringDate; }
    public BigDecimal getTotal() { return total; }
    public List<OrderItem> getItems() { return items; }
}
