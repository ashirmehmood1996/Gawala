package com.android.example.gawala.Models;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class StopMarkerModel implements Serializable {
    private String id;
    private double latitude;
    private double longitude;
    private long timeStamp;
    private Marker marker;


    public StopMarkerModel(String id, double latitude, double longitude, long timeStamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
