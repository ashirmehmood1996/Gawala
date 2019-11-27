package com.android.example.gawala.Consumer.Models;

public class ProducerModel {
    private String id, name, number;
    private String imageUri;
    private int status;
    public static final int REQUEST_SENT = 0;
    public static final int REQUEST_ACCEPTED = 1;
    public static final int STATUS_NEUTRAL = 2;


    public ProducerModel(String id, String name, String number, String imageUri) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.status = STATUS_NEUTRAL;
        this.imageUri = imageUri;

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


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
