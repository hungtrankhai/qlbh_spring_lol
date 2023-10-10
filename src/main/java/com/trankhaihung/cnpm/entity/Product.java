package com.trankhaihung.cnpm.entity;

import com.trankhaihung.cnpm.entity.audit.DateAudit;
import com.trankhaihung.cnpm.entity.enums.Status;
import org.apache.commons.math3.random.RandomDataGenerator;

import javax.persistence.*;

@Entity
@Table(name = "product")
public class Product extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "original_price")
    private Double originalPrice;

    @Column(name = "price")
    private Double price;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "long_description")
    private String longDescription;

    @Transient
    private Integer availableQuantity;

    @Transient
    private Integer quantity;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, optional = false)
    private WareHouse wareHouse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public WareHouse getWareHouse() {
        return wareHouse;
    }

    public void setWareHouse(WareHouse wareHouse) {
        this.wareHouse = wareHouse;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Transient
    public String getPathImageProduct() {
        if (imageName == null || id == null) {
            return null;
        }
        return "/product-photos/" + imageName;
    }

    @Transient
    public Integer getPositionImage() {
        if (imageName == null || id == null) {
            return null;
        }

        RandomDataGenerator generator  = new RandomDataGenerator();
        return generator.nextInt(2, 3);
    }
}
