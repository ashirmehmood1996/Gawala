package com.android.example.gawala.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        if (isInternetAvailable()) {


            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                sendUserToRelevantActiviy();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } else {
            // TODO: 6/28/2019  show no internet view
            Toast.makeText(this, "please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        super.onStart();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
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
                        startActivity(new Intent(MainActivity.this, ProducerActivity.class));
                        finish();
                    } else if (userType != null && userType.equals("consumer")) {
                        //          showProgressBar(false);
                        startActivity(new Intent(MainActivity.this, ConsumerActivity.class));
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
// TODO: 6/26/2019  make an early version of this app with in these 5 days
//  below are some abstract level steps each one need further expalanation.
//  1) make a connection to firebase // now here
//  2) initially no map is to be displayed
//  3) create a  mechanism to have connections of the consumers and providers
//  4) on the tap of a button producer will share location to consumer
//  5) consimer will simple get the values initilly then we will go for the map later
//  6) its time to add maps on both sides
//  7) now make the things funtional on the app.