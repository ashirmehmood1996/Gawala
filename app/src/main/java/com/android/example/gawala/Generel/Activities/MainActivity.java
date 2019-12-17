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
import com.android.example.gawala.Transporter.Activities.TransporterRideActivity;
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
                        finish();
                        startActivity(getIntent());

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
//fixme whenever user logs in he she shall store its usertype in shared pref may be i guess

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
            startActivity(new Intent(MainActivity.this, ProviderMainActivity.class));
            finish();
        } else if (userType != null && userType.equals(getResources().getString(R.string.consumer))) {
            //          showProgressBar(false);
            startActivity(new Intent(MainActivity.this, ConsumerMainActivity.class));
            finish();
        } else if (userType != null && userType.equals(getResources().getString(R.string.transporter))) {
            //          showProgressBar(false);
            startActivity(new Intent(MainActivity.this, TransporterRideActivity.class));
            finish();
        } else {
            Toast.makeText(MainActivity.this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();
        }
    }
}
// TODO: 11/23/2019
//  done !!!! 1) add pictures to the profiles and products add picture to the producer details activty and the to the services
//  4) !!! done show customers in real time that the request is accepted or rejected
//  6) !!! done before starting the ride. Producer should be able to see what he has to carry in dashboard fragment. the name can also be changed
//  8) !!! done set the manage vacations disable issue
//  9) improve the UI i.e. add animations plus fine tune coloring add Image to makers
//  2) later  take the repetitive notiofications module take help from the previous hints that were created by you few lines above in previous to do
//  5) later  make record of all notifitcations for both consumers and producers
//  7) later  you must make the ride up and running in background
//  3) !!? partially done make the records summery more robust like an option to fetch the monthly or yearly summery can also add the payment recived or not module also
//  10) make the notification batch working and to other places like notification button and app oicon too
//  11) add rating feature
//  12) add Categories of products add the unit of products hence make it a full fledge e commerce app
//      the admin will be the main thing that consumer will deal the rider are juts delivery boys so nithing has changed I guess
//      so we may start it quickly and make it worth it but yes we need to make a copy of this current state
//  partially done!! fourth once the milk (good)  has been delivered both parties should confirm the delivery on devices
//  later third we add repeated notifications for the approaching provider to the consumer with in the time or distance consumer wants the notificatiosn to be delivered
//      logic for third can be that we have a fixed number of notification to be send each be represented by integer and each time a variable is set/incremented that defines whihc notfifications conition is required to be checked and how many are already sent
//      e.g. 0 represents no notification sent,1 represent initial notification is sent 2 represents half way notification is sent , 3 represents 5 minutes for approach etc
//      repeated notifications can have a number of user options e.g. frequency, volume, tune selection , caller like activity alert  for final alert that has a mute option init
//  this link will be used for consumer to send request only to the nearest consumer if needed https://www.youtube.com/watch?v=jvhD7-q45_w&list=PLaoF-xhnnrRULoWAGjWJ79-BwD1mAMwB0&index=8


// TODO: 12/5/2019 Latest
//  2) First we see the provider side.
//  2.1)  !!!done place profile module
//  2.2)  !!!done place notification module
//  2.3)  !!!done place Services Module
//  2.4)  !!!done place Clients Module need a little more functionality to be shifted as the cients were prefetched either we prefetch them now whicg is memory intensive or we may cache them in sqlite or any ither form or we simple have them on the go
//  2.5) ??later we first need to do the rider portion Develop Riders Module
//        provider can also be a rider and hence by default there will be one rider available to which the cients can be assigned
//        we can also see the list of client he she is assigned for delivery and can remove the client any time from that rider
//  2.6) later(because it will be though of at the time of ride saving with special reference to old records) Modify History Module // may be we just make another parent node for each rider node and hence make the data fetching easy
//  2.7) should show the statistic on top like amazon does wich will have an initila hard codded values of clients riders earnings etc
//  3)  !!!done See client side
//  4)  see what we can do with the RIDER and other users sides too i.e. provider and client
//  4.1) !!!done transporter share his code
//  4.2) !!!done provider add the transporter in one click
//  4.3) !!!done list transporter in the transporter activity (will deal later with self transporter role)
//  4.4) !!!done  transporter can see the  provider details
//  4.5) !!!done provider should accept the client and assign it to a trnsporter and then the request is processed only
//  4.6) start riding
//  4.6.1) !!!done show clients only in one fragment and do all the data fatching there
//  4.6.2) !!! done fethc fresh data in ride fragment and start ride as it was before in a map fragment may be
//  4.6.3) shift the functionality in a service
//  4.7)  !!! done show shummery for consumer
//  4.8) !!! done show shummery for provider
//  4.9) !!! done show shummery for rider
//  5)  in provider activity show assigned transporter for the clinet and a change transporter option indeed
//  6) show the number of clients sales and success on top may be hard coded and further improve the UI



// TODO: 12/17/2019 Now
//  after that we will perform the remianing task above or may include them here
//  1) put the ride in service
//  2) make it completely online i.e. for each ride we be sending values
// what are the main fucntions of a ride:
// fetch a polyline of complete route (not necessarily)
// inform the consumer that his delivery is on board (not necessarily)
// when transporter is near to the consumer about some meters then he/ be notified that the delivery is on board(required )
// when the stuff is delivered we need to make a boolaen that delivered is true and deriver recieves an acknowledgement (required)
// data for this delivery is saved (required)
// todo   now   so first we think for the required features in service
//  1) we fetch the active rides models as we do already and get all the necessary data regarding deiveries
//  2) we make this data passed to the service or to the fire base where it resides as an active session
//  3) the is delivered option should be as a button in foreground notification  and in the ride activity which it is already there
//  4) the at the time of delivery what we do is simply make the set delivered to true for each consumer goods delivered
//  5) when activity is restarted we simple fetch the session data from service or firebase where ever the list resides base on check that there is a session already running and we make the ride active accordingly
//  6) when the ride is completed we fetch the data from session node and replace it to the data history node which in our case is summery node
//












//  fixme should make a separate activty or class for permissions asking  and send the user there each time a pemission is needed
//  fixme allow the provider in real time that client locvation has changes
