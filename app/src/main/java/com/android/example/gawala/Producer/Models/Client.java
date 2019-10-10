package com.android.example.gawala.Producer.Models;

public class Client {
    private String clientID;
    private String name;
    private float milkVolume;

    public Client(String clientID, String name, float milkVolume) {
        this.clientID = clientID;
        this.name = name;
        this.milkVolume = milkVolume;
    }

    public String getClientID() {
        return clientID;
    }

    public String getName() {
        return name;
    }

    public float getMilkVolume() {
        return this.milkVolume;
    }
}