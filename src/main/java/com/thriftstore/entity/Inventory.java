package com.thriftstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    private String name;
    private int stock; // Renamed from quantity
    private double rentPrice;
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    // Default constructor required by JPA
    public Inventory() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStock() { return stock; } // Updated getter
    public void setStock(int stock) { this.stock = stock; } // Updated setter
    public double getRentPrice() { return rentPrice; }
    public void setRentPrice(double rentPrice) { this.rentPrice = rentPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}