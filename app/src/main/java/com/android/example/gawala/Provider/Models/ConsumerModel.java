package com.android.example.gawala.Provider.Models;

import android.net.Uri;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.ArrayList;

public class ConsumerModel implements Serializable {
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
    private long alertNotificationTime;// in seconds

    public int NOTIFICATION_STATE = 0;//this represents that what amount of notifications are sent to a consumer


    private ArrayList<AcquiredGoodModel> demandArray;
    private Uri imageUri;


    public ConsumerModel(String id, String name, String numeber, String time_stamp, String lat, String lng, String imageUrl, long alertNotificationTime) {
        this.id = id;
        this.name = name;
        this.number = numeber;
        this.time_stamp = time_stamp;
        this.lat = lat;
        this.lng = lng;
        this.isDelivered = false;
        this.hasDemand = false;
        this.isOnVacation = false;
        this.imageUrl = imageUrl;
        this.alertNotificationTime = alertNotificationTime;

    }

    /**
     * @return a duplicate model that has all basic fields
     */
    public ConsumerModel getConsumerModel() {
        ConsumerModel consumerModel = new ConsumerModel(this.id, this.name, this.number, this.time_stamp, this.lat, this.lng, this.imageUrl, this.alertNotificationTime);
        consumerModel.setDelivered(this.isDelivered);
        consumerModel.setHasDemand(this.hasDemand);
        consumerModel.setOnVacation(this.isOnVacation);
        consumerModel.setLocationName(this.locationName);
        consumerModel.setDemandArray(this.demandArray);//be noted that this is refering to the original obejct of the demand array
        return consumerModel;
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

    public long getAlertNotificationTime() {
        return alertNotificationTime;
    }

    public void setAlertNotificationTime(long alertNotificationTime) {
        this.alertNotificationTime = alertNotificationTime;
    }


    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
