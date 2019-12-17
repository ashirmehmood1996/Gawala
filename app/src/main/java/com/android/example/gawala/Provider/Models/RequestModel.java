package com.android.example.gawala.Provider.Models;

import java.io.Serializable;

public class RequestModel implements Serializable {
    private String sender_id;
    private String name;
    private String time_stamp;
    private String lat;
    private String lng;
    private String number;
    private String address;
    private String imageUrl;


    public RequestModel(String sender_id, String name, String number, String time_stamp, String lat, String lng, String imageUrl) {
        this.sender_id = sender_id;
        this.name = name;
        this.number = number;
        this.time_stamp = time_stamp;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
    }

    public String getNumber() {
        return number;


    }

    public String getName() {
        return name;
    }

    public String getSender_id() {
        return sender_id;
    }


    public String getTime_stamp() {
        return time_stamp;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
