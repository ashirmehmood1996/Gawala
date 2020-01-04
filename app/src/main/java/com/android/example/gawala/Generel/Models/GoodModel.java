package com.android.example.gawala.Generel.Models;

import java.io.Serializable;

public class GoodModel implements Serializable {
    private String name, description, price, type, image_uri, unit;//more fields will be added later according to the need
    private String id;

    public GoodModel() {
    }

    public GoodModel(String name, String description, String price, String type, String image_uri, String unit) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.image_uri = image_uri;
        this.unit = unit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
