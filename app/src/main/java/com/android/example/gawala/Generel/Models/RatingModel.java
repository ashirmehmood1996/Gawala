package com.android.example.gawala.Generel.Models;

public class RatingModel {
    private String rating;
    private String receiver_id;
    private String time_stamp;

    private String rater_id;
    private String rater_name;
    private String description;

    public RatingModel() {
    }

    public RatingModel(String rating, String receiver_id, String time_stamp, String rater_id, String rater_name, String description) {
        this.rating = rating;
        this.receiver_id = receiver_id;
        this.time_stamp = time_stamp;
        this.rater_id = rater_id;
        this.rater_name = rater_name;
        this.description = description;
    }

    public String getRating() {
        return rating;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void update(RatingModel ratingModel) {
        this.rating = ratingModel.getRating();
        this.receiver_id = ratingModel.getReceiver_id();
        this.time_stamp = ratingModel.getTime_stamp();
        this.rater_id = ratingModel.getRater_id();
        this.rater_name = ratingModel.getRater_name();
        this.description = ratingModel.getDescription();

    }

    public String getRater_id() {
        return rater_id;
    }

    public String getRater_name() {
        return rater_name;
    }

    public String getDescription() {
        return description;
    }
}
