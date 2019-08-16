package com.android.example.gawala;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_1_ID = "GawalaChannelOne";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        //if current adroid os is oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create a new notificationChannel
            NotificationChannel notificationChannelForServices = new NotificationChannel(
                    CHANNEL_1_ID,//with  this id
                    "Download Service Notification Cahannel", //with this name
                    NotificationManager.IMPORTANCE_HIGH //with this priority
            );
            //setting description of that channel
            notificationChannelForServices.setDescription("This notificationChannel is for displaying alet to consumer that the delivery is about to be arrived");

            //getting reference of Notification manager
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //registering my channel to the Notification Manager
            notificationManager.createNotificationChannel(notificationChannelForServices);

            //now register this class in manifest by adding the app name to manifest so it will wrap up the whole application
        }
    }
}
