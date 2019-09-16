package com.android.example.gawala.Models;

import com.google.android.gms.maps.model.Marker;

public class ConsumerModel {
    private String id;
    private String name;
    private String number;

    private String time_stamp;
    private String lat;
    private String lng;
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public ConsumerModel(String id, String name, String numebr, String time_stamp, String lat, String lng) {
        this.id = id;
        this.name = name;
        this.number = numebr;
        this.time_stamp = time_stamp;
        this.lat = lat;
        this.lng = lng;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getNumber() {
        return number;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLatitude() {
        return lat;
    }

    public String getLongitude() {
        return lng;
    }

    public String getTime_stamp() {
        return time_stamp;
    }
}
