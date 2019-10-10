package com.android.example.gawala.Generel.Models;

import java.io.Serializable;

public class GoodModel implements Serializable {
    private String name,description,price,type;//more fields will be added later according to the need
    private String id;

    public GoodModel(){}

    public GoodModel(String name, String description, String price, String type) {
        this.name = name;
        this.description = description;
        this.price = price;
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
}
