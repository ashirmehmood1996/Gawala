package com.android.example.gawala.Producer.Models;

import java.util.ArrayList;

public class ProducerSummeryModel {
    private String sessionID;
    private int milkPrice;
    private long timeStamp;
    private ArrayList<Client> clientArrayList;
    private float totalMilkVolume;
    private float totalMilkamount;

    public ProducerSummeryModel(String sessionID, int milkPrice, long timeStamp, ArrayList<Client> clientArrayList) {
        this.sessionID = sessionID;
        this.milkPrice = milkPrice;
        this.timeStamp = timeStamp;
        this.clientArrayList = clientArrayList;
        this.totalMilkVolume = getTotalVolume(clientArrayList);
        this.totalMilkamount = totalMilkVolume * milkPrice;
    }

    private float getTotalVolume(ArrayList<Client> clientArrayList) {
        float totalVolume=0.0f;
        for (Client client:clientArrayList){
            totalVolume+=client.getMilkVolume();
        }
        return totalVolume;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getMilkPrice() {
        return milkPrice;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public ArrayList<Client> getClientArrayList() {
        return clientArrayList;
    }

    public float getTotalMilkVolume() {
        return totalMilkVolume;
    }

    public float getTotalMilkamount() {
        return totalMilkamount;
    }


}
