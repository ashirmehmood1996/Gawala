package com.android.example.gawala.Generel.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Activities.ConsumerDashBoardActivity;
import com.android.example.gawala.Producer.Activities.ProducerNavMapActivity;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private LinearLayout alertContanerLieanrLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertContanerLieanrLayout = findViewById(R.id.ll_main_alert_container);
        progressBar = findViewById(R.id.pb_main);

    }

    @Override
    protected void onStart() {
        if (isInternetAvailable()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                sendUserToRelevantActiviy();
            } else {
                finish();
                startActivity(new Intent(this, LoginActivity.class));
            }
        } else {
            progressBar.setVisibility(View.GONE);
            alertContanerLieanrLayout.setVisibility(View.VISIBLE);
            alertContanerLieanrLayout.findViewById(R.id.bt_main_refresh)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                            startActivity(getIntent());

                        }
                    });
            Toast.makeText(this, "please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        super.onStart();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void sendUserToRelevantActiviy() {


        FirebaseUser curuntUser = FirebaseAuth.getInstance().getCurrentUser();
        if (curuntUser != null) {//now user is logged in
            //we check that user type and send the user to its respective activity
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userType = dataSnapshot.getValue(String.class);
                    if (userType != null && userType.equals("producer")) {
                        //            showProgressBar(false);
                        startActivity(new Intent(MainActivity.this, ProducerNavMapActivity.class));
                        finish();
                    } else if (userType != null && userType.equals("consumer")) {
                        //          showProgressBar(false);
                        startActivity(new Intent(MainActivity.this, ConsumerDashBoardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //    showProgressBar(false);
                    Toast.makeText(MainActivity.this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }
}


// TODO: 7/27/2019 later in a fragment or activity we can show near by producers and their image rates distance, reviews, along with an option for connection request and adjust timmings ..

//TODO: 10/25/2019 now stat from login signup to the summery of data and modify/ upgrade /  the functionalities to make app fully fucntional and make ui also promising
//  the following thing are undergoing
//  make the pickup point activty have search location option and show the location as string for the purpose the youtube videos will help out e.g.  https://www.youtube.com/watch?v=RBY4PTjXHBc
//  now here https://www.youtube.com/watch?v=wOVDnoaUNsY&list=PLaoF-xhnnrRULoWAGjWJ79-BwD1mAMwB0&index=4
//  this link will be used for consumer to send request only to the nearest consumer if needed https://www.youtube.com/watch?v=jvhD7-q45_w&list=PLaoF-xhnnrRULoWAGjWJ79-BwD1mAMwB0&index=8

// TODO: 10/27/2019 sequece
//  done!!! first we let the user pick up their location at which they want the delivery to be recieved
//  partially done!! fourth once the milk (good)  has been delivered both parties should confirm the delivery on devices
//  done!!! fifth add option to let the user decide dates on a calender on which he/she does not want the delivery and it should be reversible just in case
//  done!!! allow the consumer only to edit his location , remove option from provider
//  here ???
//  later second3 we let the provider decide an approximate radius or polyline in which he will deliver its services still if it gets request that covers an area where producer is not willing to deliver then he can simply reject the request of client
//  later third we add repeated notifications for the approaching provider to the consumer with in the time or distance consumer wants the notificatiosn to be delivered
//      logic for third can be that we have a fixed number of notification to be send each be represented by integer and each time a variable is set/incremented that defines whihc notfifications conition is required to be checked and how many are already sent
//      e.g. 0 represents no notification sent,1 represent initial notification is sent 2 represents half way notification is sent , 3 represents 5 minutes for approach etc
//      repeated notifications can have a number of user options e.g. frequency, volume, tune selection , caller like activity alert  for final alert that has a mute option init
//  later seventh improve UI and add extra animation features if time




