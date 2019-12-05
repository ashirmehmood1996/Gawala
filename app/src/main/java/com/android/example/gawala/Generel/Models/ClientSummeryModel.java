package com.android.example.gawala.Generel.Models;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;

import java.util.ArrayList;

public class ClientSummeryModel {
    private String clientID;
    private String name;
    private float totalCost;
    private long time_stamp;
    private ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList;

    public ClientSummeryModel(String clientID, String name, ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList) {
        this.clientID = clientID;
        this.name = name;
        this.acquiredGoodModelArrayList = acquiredGoodModelArrayList;
        this.totalCost = calculateTotalCost();
    }

    private float calculateTotalCost() {
        float cost = 0.0f;
        for (AcquiredGoodModel acquiredGoodModel : acquiredGoodModelArrayList) {
            int demandUnits = Integer.parseInt(acquiredGoodModel.getDemand());
            int pricePerUnit = Integer.parseInt(acquiredGoodModel.getGoodModel().getPrice());
            int totalPrice = demandUnits * pricePerUnit;
            cost = cost + totalPrice;
        }
        return cost;
    }

    public String getClientID() {
        return clientID;
    }

    public String getName() {
        return name;
    }

    public float getTotalCost() {
        return totalCost;
    }

    public ArrayList<AcquiredGoodModel> getAcquiredGoodModelArrayList() {
        return acquiredGoodModelArrayList;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }
}