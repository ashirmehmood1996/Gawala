package com.android.example.gawala.Activities;

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

import com.android.example.gawala.ProducerNavMapActivity;
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
//todo later divide user info in pulic and private pulis is demnded by any user but private only by the current user