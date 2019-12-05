package com.android.example.gawala.Producer.Models;

import com.android.example.gawala.Generel.Models.ClientSummeryModel;

import java.util.ArrayList;

public class ProducerSummeryModel {
    private String sessionID;
    private long timeStamp;
    private ArrayList<ClientSummeryModel> clientSummeryModelArrayList;
    //    private float totalMilkVolume;
    private float totalAmount;

    public ProducerSummeryModel(String sessionID, long timeStamp, ArrayList<ClientSummeryModel> clientSummeryModelArrayList) {
        this.sessionID = sessionID;

        this.timeStamp = timeStamp;
        this.clientSummeryModelArrayList = clientSummeryModelArrayList;

        this.totalAmount = calculateTalAmount();
    }

    private float calculateTalAmount() {
        float totalAmount = 0;
        for (ClientSummeryModel clientSummeryModel : clientSummeryModelArrayList){
            totalAmount+= clientSummeryModel.getTotalCost();
        }
        return totalAmount;
    }

    public String getSessionID() {
        return sessionID;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public ArrayList<ClientSummeryModel> getClientSummeryModelArrayList() {
        return clientSummeryModelArrayList;
    }


    public float getTotalAmount() {
        return totalAmount;
    }


}
