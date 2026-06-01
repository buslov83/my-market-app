package ru.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
public class Product {

    @Id
    private Long id;

    private String title;

    private String description;

    @Column("img_path")
    private String imgPath;

    private long price;

    @Column("external_id")
    private String externalId;

    public Product() {
    }

    public Product(String title, String description, String imgPath, long price) {
        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
    }

    public Product(String title, String description, String imgPath, long price, String externalId) {
        this(title, description, imgPath, price);
        this.externalId = externalId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
