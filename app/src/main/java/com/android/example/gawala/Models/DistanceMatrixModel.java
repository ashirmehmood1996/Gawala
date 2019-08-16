package com.android.example.gawala.Models;

public class DistanceMatrixModel {
    private String startLocation;
    private String endLocation;
    private String distance;
    private String duration;
    private long distanceLong;
    private long durationLong;

    public DistanceMatrixModel(String startLocation, String endLocation, String distance, String duration, long distanceLong, long durationLong) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.duration = duration;
        this.distanceLong = distanceLong;
        this.durationLong = durationLong;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public long getDistanceLong() {
        return distanceLong;
    }

    public long getDurationLong() {
        return durationLong;
    }
}
