package com.android.example.gawala.Producer.Models;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class ConsumerModel{ // FIXME: 11/24/2019 there is a  saerialization problem when ever i open another actiivty while the dashboard fragment is open
    // fixme later we will avoid doing it  by directly getting the arraylist from activity with a oublci method
    private String id;
    private String name;
    private String number;

    private String time_stamp;
    private String lat;
    private String lng;
    private Marker marker;
    private String locationName;
    private String imageUrl;

    //changing data
    private float amountOfMilk;
    private boolean isDelivered;
    private boolean hasDemand;
    private boolean isOnVacation;

    public int NOTIFICATION_STATE=0;//this represents that what amount of notifications are sent to a consumer


    private ArrayList<AcquiredGoodModel> demandArray;


    public ConsumerModel(String id, String name, String numebr, String time_stamp, String lat, String lng,String imageUrl) {
        this.id = id;
        this.name = name;
        this.number = numebr;
        this.time_stamp = time_stamp;
        this.lat = lat;
        this.lng = lng;
        this.isDelivered=false;
        this.hasDemand=false;
        this.isOnVacation=false;
        this.imageUrl=imageUrl;

    }

    public ArrayList<AcquiredGoodModel> getDemandArray() {
        return demandArray;
    }

    public void setDemandArray(ArrayList<AcquiredGoodModel> demandArray) {
        this.demandArray = demandArray;
    }


    public boolean hasDemand() {
        return hasDemand;
    }

    public void setHasDemand(boolean hasDemand) {
        this.hasDemand = hasDemand;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getNumber() {
        return number;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLatitude() {
        return lat;
    }

    public String getLongitude() {
        return lng;
    }

    public String getTime_stamp() {
        return time_stamp;
    }
    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public float getAmountOfMilk() {
        return amountOfMilk;
    }

    public void setAmountOfMilk(float amountOfMilk) {
        this.amountOfMilk = amountOfMilk;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public boolean isOnVacation() {
        return isOnVacation;
    }

    public void setOnVacation(boolean onVacation) {
        isOnVacation = onVacation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
