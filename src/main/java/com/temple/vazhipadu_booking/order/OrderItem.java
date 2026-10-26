package com.temple.vazhipadu_booking.order;

import com.temple.vazhipadu_booking.offering.Offering;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @Column(nullable = false)
    private int quantity;

    // Preserve the price charged when the order was placed.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private TempleOrder order;

    protected OrderItem() {
    }

    public OrderItem(Offering offering, int quantity,
                     BigDecimal unitPrice) {
        this.offering = offering;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void setOrder(TempleOrder order) {
        this.order = order;
    }

    public Offering getOffering() { return offering; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
