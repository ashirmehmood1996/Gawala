package com.android.example.gawala.Consumer.Models;

public class ProducerModel {
    private String id, name, number;
    private int status;
    public  static final  int REQUEST_SENT=0;
    public  static final  int REQUEST_ACCEPTED=1;
    public  static final  int NEUTRAL=2;


    public ProducerModel(String id, String name, String number) {
        this.id = id;
        this.name = name;
        this.number = number;
        status=NEUTRAL;
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
}
