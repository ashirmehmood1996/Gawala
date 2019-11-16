package com.android.example.gawala.Generel.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.example.gawala.Generel.Adapters.NotificationsAdapter;
import com.android.example.gawala.Generel.Models.NotificationModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.Callback {

    private RecyclerView recyclerView;
    private NotificationsAdapter notificationsAdapter;
    private ArrayList<NotificationModel> notificationModelArrayList;

    private DatabaseReference rootRef;
    private String myId;
    private AlertDialog mAlertDialog;

    private RelativeLayout emptyViewcontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        getSupportActionBar().setTitle("Notifications");

        initFields();
        loadNotifications();

    }


    private void initFields() {
        recyclerView = findViewById(R.id.rv_notification);
        notificationModelArrayList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationModelArrayList, this);
        recyclerView.setAdapter(notificationsAdapter);
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        emptyViewcontainer = findViewById(R.id.rl_notification_empty_view_container);

        initializeDialog();
    }

    private void loadNotifications() {
        mAlertDialog.show();
        rootRef.child("notifications")
                .child(myId)//reciever id
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            notificationModelArrayList.clear();
                            for (DataSnapshot notificationsSnap : dataSnapshot.getChildren()) {
                                String senderId = notificationsSnap.getKey();
                                for (DataSnapshot childNotiiftionSnap : notificationsSnap.getChildren()) {
                                    String notificationId = childNotiiftionSnap.getKey();
                                    String title = childNotiiftionSnap.child("title").getValue(String.class);
                                    String message = childNotiiftionSnap.child("message").getValue(String.class);
                                    String timeStamp = childNotiiftionSnap.child("time_stamp").getValue(String.class);//this may be null so deal bufddy
                                    String type = childNotiiftionSnap.child("type").getValue(String.class);
                                    NotificationModel notificationModel = new NotificationModel(notificationId, senderId, title, type, message, timeStamp);
                                    notificationModelArrayList.add(notificationModel);
                                }
                            }
                            notificationsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(NotificationsActivity.this, "No notifications yet", Toast.LENGTH_SHORT).show();

                        }
                        if (notificationModelArrayList.size() == 0) {
                            emptyViewcontainer.setVisibility(View.VISIBLE);
                        } else {
                            emptyViewcontainer.setVisibility(View.GONE);
                        }
                        mAlertDialog.cancel();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    @Override
    public void onNotificationItemClicked(int pos) {


    }
}
