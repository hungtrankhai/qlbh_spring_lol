package com.trankhaihung.cnpm.dto;

public class CartResponseDTO {
    private double subTotal;
    private double shipPrice;

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getShipPrice() {
        return shipPrice;
    }

    public void setShipPrice(double shipPrice) {
        this.shipPrice = shipPrice;
    }
}
