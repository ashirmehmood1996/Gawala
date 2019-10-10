package com.android.example.gawala.Producer.Models;

public class RequestModel {
    private String sender_id;
    private String name;
    private String time_stamp;
    private String location_lat;
    private String location_lon;
    private String number;

    public RequestModel(String sender_id, String name, String number, String time_stamp, String location_lat, String location_lon) {
        this.sender_id = sender_id;
        this.name = name;
        this.number = number;
        this.time_stamp = time_stamp;
        this.location_lat = location_lat;
        this.location_lon = location_lon;
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

    public String getLocation_lat() {
        return location_lat;
    }

    public String getLocation_lon() {
        return location_lon;
    }
}
