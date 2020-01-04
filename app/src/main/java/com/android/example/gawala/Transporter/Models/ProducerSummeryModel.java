package com.android.example.gawala.Transporter.Models;

import com.android.example.gawala.Generel.Models.ClientSummeryModel;

import java.util.ArrayList;

public class ProducerSummeryModel {
    private String sessionID,transporterName;
    private long timeStamp;
    private ArrayList<ClientSummeryModel> clientSummeryModelArrayList;
    //    private float totalMilkVolume;
    private float totalAmount;

    public ProducerSummeryModel(String sessionID, String transporterName, long timeStamp, ArrayList<ClientSummeryModel> clientSummeryModelArrayList) {
        this.sessionID = sessionID;
        this.transporterName = transporterName;
        this.timeStamp = timeStamp;
        this.clientSummeryModelArrayList = clientSummeryModelArrayList;
        this.totalAmount = calculateTotalAmount();
    }

    private float calculateTotalAmount() {
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


    public String getTransporterName() {
        return transporterName;
    }
}
