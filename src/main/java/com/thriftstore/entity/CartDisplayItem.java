package com.thriftstore.entity;

public class CartDisplayItem {
    private Inventory inventory;
    private int quantity;

    // Getters and Setters
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}