package com.android.example.gawala.Generel.Models;

public class NotificationModel {
    private String notificationId;
    private String senderId;
    private String notificationTitle;
    private String notificationType;
    private String notificationMessage;
    private String timeStamp;

    public NotificationModel(String notificationId, String senderId, String notificationTitle, String notificationType, String notificationMessage, String timeStamp) {
        this.notificationId = notificationId;
        this.senderId = senderId;
        this.notificationTitle = notificationTitle;
        this.notificationType = notificationType;
        this.notificationMessage = notificationMessage;
        this.timeStamp = timeStamp;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
