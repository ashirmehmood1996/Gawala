package com.android.example.gawala.Generel.Models;

import android.app.Service;

import java.io.Serializable;

public class ConTransporterModel implements Serializable {

    private String transporterId, transporterName, providerId, providerName;

    public ConTransporterModel(String transporterId, String transporterName, String providerId, String providerName) {
        this.transporterId = transporterId;
        this.transporterName = transporterName;
        this.providerId = providerId;
        this.providerName = providerName;
    }

    public String getTransporterId() {
        return transporterId;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }
}
