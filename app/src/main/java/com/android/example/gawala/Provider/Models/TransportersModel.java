package com.android.example.gawala.Provider.Models;

public class TransportersModel {

    private String id;
    private String name;
    private String number;
    private String imageUrl;

    public TransportersModel(String id, String name, String number, String imageUrl) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
