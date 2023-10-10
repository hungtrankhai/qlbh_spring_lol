package com.trankhaihung.cnpm.entity;

import com.trankhaihung.cnpm.entity.enums.Status;

import javax.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    private Double revenue;

    public Double getRevenue() {
        return revenue = product.getPrice() * quantity;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

        private Double earning;

    public Double getEarning() {
        return earning = (product.getPrice() - product.getOriginalPrice()) * quantity;
    }

    public void setEarning(Double earning) {
        this.earning = earning;
    }


}
