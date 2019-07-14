package com.android.example.gawala.Models;

public class ConnectedConsumersModel {
    private String id;
    private String name;
    private String location_lat;
    private String location_lon;

    public ConnectedConsumersModel(String id, String name, String location_lat, String location_lon) {
        this.id = id;
        this.name = name;
        this.location_lat = location_lat;
        this.location_lon = location_lon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation_lat() {
        return location_lat;
    }

    public String getLocation_lon() {
        return location_lon;
    }
}