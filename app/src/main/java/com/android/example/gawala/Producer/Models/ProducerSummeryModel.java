package com.android.example.gawala.Producer.Models;

import com.android.example.gawala.Generel.Models.ClientSummery;

import java.util.ArrayList;

public class ProducerSummeryModel {
    private String sessionID;
    private long timeStamp;
    private ArrayList<ClientSummery> clientSummeryArrayList;
    //    private float totalMilkVolume;
    private float totalAmount;

    public ProducerSummeryModel(String sessionID, long timeStamp, ArrayList<ClientSummery> clientSummeryArrayList) {
        this.sessionID = sessionID;

        this.timeStamp = timeStamp;
        this.clientSummeryArrayList = clientSummeryArrayList;

        this.totalAmount = calculateTalAmount();
    }

    private float calculateTalAmount() {
        float totalAmount = 0;
        for (ClientSummery clientSummery : clientSummeryArrayList){
            totalAmount+= clientSummery.getTotalCost();
        }
        return totalAmount;
    }

    public String getSessionID() {
        return sessionID;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public ArrayList<ClientSummery> getClientSummeryArrayList() {
        return clientSummeryArrayList;
    }


    public float getTotalAmount() {
        return totalAmount;
    }


}
