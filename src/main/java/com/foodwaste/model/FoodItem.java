package com.foodwaste.model;

import java.io.Serializable;

/**
 * FoodItem represents a food donation/waste entity in the Food Waste Management System.
 * Encapsulates item attributes and provides getter/setter methods following standard OOP encapsulation.
 */
public class FoodItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int itemId;
    private int providerId;
    private String name;
    private String category;
    private double quantity;
    private String unit;
    private String expirationDate;
    private String status;

    public FoodItem() {
    }

    public FoodItem(int itemId, int providerId, String name, double quantity, String expirationDate, String status) {
        this.itemId = itemId;
        this.providerId = providerId;
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format(
            "FoodItem[ID=%d, Provider=%d, Name='%s', Qty=%.2f %s, Expiry='%s', Status='%s']",
            itemId, providerId, name, quantity, (unit != null ? unit : ""), expirationDate, status
        );
    }
}
