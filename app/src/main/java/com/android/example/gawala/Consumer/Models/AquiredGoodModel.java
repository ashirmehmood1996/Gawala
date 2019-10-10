package com.android.example.gawala.Consumer.Models;

import com.android.example.gawala.Generel.Models.GoodModel;

import java.io.Serializable;

public class AquiredGoodModel implements Serializable {
    private String demand;
    private String producerId;
    private GoodModel goodModel;

    public AquiredGoodModel(String demand, String producerId, GoodModel goodModel) {
        this.demand = demand;
        this.producerId = producerId;
        this.goodModel = goodModel;
    }

    public String getDemand() {
        return demand;
    }


    public GoodModel getGoodModel() {
        return goodModel;
    }

    public String getProducerId() {
        return producerId;
    }
}
