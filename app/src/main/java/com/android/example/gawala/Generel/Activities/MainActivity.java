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

import com.android.example.gawala.Consumer.Activities.ConsumerMainActivity;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Provider.Activities.ProviderMainActivity;
import com.android.example.gawala.R;
import com.android.example.gawala.Transporter.Activities.TransporterMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private LinearLayout alertContainerLieanrLayout;
    private ProgressBar progressBar;

    public static DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("triplet");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertContainerLieanrLayout = findViewById(R.id.ll_main_alert_container);
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
            alertContainerLieanrLayout.setVisibility(View.VISIBLE);
            alertContainerLieanrLayout.findViewById(R.id.bt_main_refresh)
                    .setOnClickListener(v -> {
                        recreate();

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

            String userType = SharedPreferenceUtil.getValue(getApplicationContext(), getResources().getString(R.string.type_key));
            if (userType != null) {
                sendUsingType(userType);
                return;
            }

            rootRef.child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (MainActivity.this != null) {
                        String userType = dataSnapshot.getValue(String.class);
                        sendUsingType(userType);
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

    private void sendUsingType(String userType) {
        if (userType != null && userType.equals(getResources().getString(R.string.provider))) {
            //            showProgressBar(false);
            if ((SharedPreferenceUtil.hasValue(getApplicationContext(), getResources().getString(R.string.mode_key))) &&
                    (SharedPreferenceUtil.getValue(getApplicationContext(), getResources()
                            .getString(R.string.mode_key)).equals(getResources().getString(R.string.transporter)))) {
                Intent intent = new Intent(this, TransporterMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(TransporterMainActivity.IS_PROVIDER_TOO, true);
                startActivity(intent);
            } else {
                startActivity(new Intent(MainActivity.this, ProviderMainActivity.class));
            }
            finish();
        } else if (userType != null && userType.equals(

                getResources().

                        getString(R.string.consumer))) {
            //          showProgressBar(false);
            startActivity(new Intent(MainActivity.this, ConsumerMainActivity.class));
            finish();
        } else if (userType != null && userType.equals(

                getResources().

                        getString(R.string.transporter))) {
            //          showProgressBar(false);
            startActivity(new Intent(MainActivity.this, TransporterMainActivity.class));
            finish();
        } else {
            Toast.makeText(MainActivity.this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();
        }
    }
}
//  this link will be used for consumer to send request only to the nearest consumer if needed https://www.youtube.com/watch?v=jvhD7-q45_w&list=PLaoF-xhnnrRULoWAGjWJ79-BwD1mAMwB0&index=8
// TODO: 11/23/2019
//   1)!!! done add pictures to the profiles and products add picture to the producer details activty and the to the services
//  4) !!! done show customers in real time that the request is accepted or rejected
//  6) !!! done before starting the ride. Producer should be able to see what he has to carry in dashboard fragment. the name can also be changed
//  8) !!! done set the manage vacations disable issue
//  9) !!!done improve the UI i.e. add animations plus fine tune coloring add Image to makers
//  7) !!!done  you must make the ride up and running in background
//  11) !!!done add rating feature

// TODO: 12/5/2019
//  2) First we see the provider side.
//  2.1)  !!!done place profile module
//  2.2)  !!!done place notification module
//  2.3)  !!!done place Services Module
//  2.4)  !!!done place Clients Module need a little more functionality to be shifted as the cients were prefetched either we prefetch them now whicg is memory intensive or we may cache them in sqlite or any ither form or we simple have them on the go
//  3)  !!!done See client side
//  4)!!!done   see what we can do with the RIDER and other users sides too i.e. provider and client
//  4.1) !!!done transporter share his code
//  4.2) !!!done provider add the transporter in one click
//  4.3) !!!done list transporter in the transporter activity (will deal later with self transporter role)
//  4.4) !!!done  transporter can see the  provider details
//  4.5) !!!done provider should accept the client and assign it to a trnsporter and then the request is processed only
//  4.6)!!!done  start riding
//  4.6.1) !!!done show clients only in one fragment and do all the data fatching there
//  4.6.2) !!! done fethc fresh data in ride fragment and start ride as it was before in a map fragment may be
//  4.6.3)!!!done  shift the functionality in a service
//  4.7)  !!! done show shummery for consumer
//  4.8) !!! done show shummery for provider
//  4.9) !!! done show shummery for rider
//  !!!done partially done make the records summery more robust like an option to fetch the monthly or yearly summery



// TODO: 12/31/2019
//fixme find aother todos and  fixmes in app
//fixme allow the provider in real time that client location has changes
//fixme make the notification batch working and to other places like notification button and app oicon too
// TODO: 8/6/2019  put a braod cast receiver when GPs is turned on adn off and then trigger the location api
// TODO: 11/16/2019  test by adding more producers
//// FIXME: 1/2/2020 the bug of no city in edit cities fragment
// FIXME: 1/6/2020 fix the bug of polyline not removed when ride was finished not happens all the times
// FIXME: 1/6/2020 on what basis the priority of stops is getting defined ?? rethink that concept and present the solution

//  1) later if said so show the consumer top items and a search option to avail new Item for which ofcourse we have to provide rge categoris and change the schema a little bit and learn some good seraching techniques good luck
//  2) later if said so should show the statistic on top like amazon does wich will have an initila hard codded values of clients riders earnings etc