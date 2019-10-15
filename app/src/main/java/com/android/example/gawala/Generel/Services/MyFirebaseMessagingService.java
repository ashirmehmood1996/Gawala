package com.android.example.gawala.Generel.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;

import com.android.example.gawala.Generel.Activities.MainActivity;
import com.android.example.gawala.Generel.App;
import com.android.example.gawala.R;
import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            UtilsMessaging.sendRegistrationToServer(s);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            switch (data.get("type")) {
                case "newRequest":
                    newRequest(remoteMessage.getData());
                    break;
                case "requestAccepted":
                    onRequestAccepted();
                    break;
                case "generel":
                    showNotification(data);
                    break;


            }
        }


    }

    private void newRequest(Map<String, String> data) {

        String sender_id = data.get("sender_id");
        String sender_name = data.get("sender_name");
        String sender_number = data.get("sender_number");
        String time_stamp = data.get("time_stamp");//set time stamp to notification if possible

        //make use of other details if needed later
        sendNotification("new ClientSummery Request ", "you have a new client request from" + sender_name);


    }

    private void onRequestAccepted() {
        sendNotification("congratulations", "You are now successfully connected to your requested produced");
    }


    // TODO: 8/17/2019 see ehat this link has to say
    //  https://firebase.google.com/docs/cloud-messaging/android/client#sample-play
    // TODO: 8/17/2019 also see ehat this link has to say
    //  https://firebase.google.com/docs/cloud-messaging/android/client#prevent-auto-init
    public void sendNotification(String title, String TransitionType) {

        //creating an intent and passing it through a pending intent which will be called when notification is clicked
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(//we can also call getService or broadcast reciever etc
                this, //context
                0, //id for pending intent if we want to cancel it later
                activityIntent, //intent to be executed by the notification
                0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_add_black_24dp)
                        .setContentTitle(title)
                        .setContentIntent(contentIntent)
                        .setContentText(TransitionType)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{1000, 1000, 1000, 1000});


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(101, mBuilder.build());
    }

    private void showNotification(Map<String, String> map) {
        // TODO: 8/4/2019  for now generating it in my own app later a node will be updated to notify the consumer usogn cloud fucntions further processing will be done

        //creating an intent and passing it through a pending intent which will be called when notification is clicked
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(//we can also call getService or broadcast reciever etc
                this, //context
                0, //id for pending intent if we want to cancel it later
                activityIntent, //intent to be executed by the notification
                0);
        //creating a notification
        NotificationCompat.Builder downloadNotificationBuilder;
        downloadNotificationBuilder = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_add_location_black_24dp)
                .setContentTitle(map.get("title"))
                .setContentText(map.get("message"))
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(11, downloadNotificationBuilder.build());

    }

}
