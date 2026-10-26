package com.temple.vazhipadu_booking.offering;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "offerings")
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;
    
    @Column(name = "daily_limit")
    private Integer dailyLimit; // null means unlimited

    public Integer getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Integer dailyLimit) {
        if (dailyLimit != null && dailyLimit < 1) {
            throw new IllegalArgumentException(
                "Daily limit must be positive"
            );
        }
        this.dailyLimit = dailyLimit;
    }

    protected Offering() {
    }

    public Offering(String name, BigDecimal price, boolean active) {
        this.name = name;
        this.price = price;
        this.active = active;
    }
    
    public Offering(String name, BigDecimal price, boolean active,Integer dailyLimit) {
        this.name = name;
        this.price = price;
        this.active = active;
        this.dailyLimit=dailyLimit;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setActive(boolean active) { this.active = active; }
}

