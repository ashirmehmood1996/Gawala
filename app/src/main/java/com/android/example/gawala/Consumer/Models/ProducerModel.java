package com.android.example.gawala.Consumer.Models;

public class ProducerModel {
    private String id, name, number;

    public ProducerModel(String id, String name, String number) {
        this.id = id;
        this.name = name;
        this.number = number;
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


}
