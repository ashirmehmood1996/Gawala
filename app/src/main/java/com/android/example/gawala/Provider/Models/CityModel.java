package com.android.example.gawala.Provider.Models;

public class CityModel {
    private String country, city;

    public CityModel(String country, String city) {
        this.country = country;
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }
}
