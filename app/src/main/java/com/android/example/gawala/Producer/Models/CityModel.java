package com.android.example.gawala.Producer.Models;

public class CityModel {
    private String id, country, city;

    public CityModel(String id, String country, String city) {
        this.id = id;
        this.country = country;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }
}
