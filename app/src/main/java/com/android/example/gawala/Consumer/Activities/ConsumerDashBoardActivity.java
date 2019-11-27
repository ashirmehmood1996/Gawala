package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.Generel.Activities.LoginActivity;
import com.android.example.gawala.Generel.Activities.NotificationsActivity;
import com.android.example.gawala.Generel.Activities.PersonalInfoActivity;
import com.android.example.gawala.Generel.Activities.PickLocationMapsActivity;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Producer.Activities.ProducerNavMapActivity;
import com.android.example.gawala.R;
import com.android.example.gawala.Consumer.Utils.ConsumerFirebaseHelper;
import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.android.example.gawala.Consumer.fragments.ClientSummeryFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ConsumerDashBoardActivity extends AppCompatActivity implements View.OnClickListener {
    private Button offdeliveryDaysButton, showSummeryButton, myProvidesButton,
            myServicesButton, /*showMapButton,*/
            myProfilebutton, notificationsButton;

    //firbase related
    private DatabaseReference rootRef;
    private String myId;
    private String producerId;
    //    private String producuerKey;
//    private String producerStatus;
//    private String mCurrentMilkPrice;
    private boolean mIsAtHome;
    private String SUMMERY_FRAG_TAG = "SummerFragmentTag";
    private AlertDialog mAlertDialog;
    private int RC_SET_DELIVERY_LOCATION = 122;

    private final int RC_PERMISSION_ALL = 100;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_dash_board);

        getSupportActionBar().setTitle("Consumer");

        initFilds();
        attachListeners();

        if (!hasPermissions(this, PERMISSIONS)) {
            requestAllPermissions();
        } else {
            checkIfUserIsCoonnectedToProducer();
            checkIfDeliveryLocationIsProvided();
        }


        UtilsMessaging.initFCM();

    }


    private void initFilds() {
        offdeliveryDaysButton = findViewById(R.id.bt_con_dash_off_delivery_days);
        showSummeryButton = findViewById(R.id.bt_con_dash_show_summery);
        myProvidesButton = findViewById(R.id.bt_con_dash_my_providers);
        myServicesButton = findViewById(R.id.bt_con_dash_my_services);
//        showMapButton = findViewById(R.id.bt_con_show_map);
        myProfilebutton = findViewById(R.id.bt_con_dash_my_profile);
        notificationsButton = findViewById(R.id.bt_con_dash_notifications);


        //date related
        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initializeDialog();
    }

    private void attachListeners() {
        offdeliveryDaysButton.setOnClickListener(this);
        showSummeryButton.setOnClickListener(this);
        myProvidesButton.setOnClickListener(this);
        myServicesButton.setOnClickListener(this);
//        showMapButton.setOnClickListener(this);
        myProfilebutton.setOnClickListener(this);
        notificationsButton.setOnClickListener(this);
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestAllPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Allow Permissions and turn on location")
                    .setCancelable(false)
                    .setMessage("In order for this app to function properly, storage and location permissions must be granted")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ActivityCompat.requestPermissions(ConsumerDashBoardActivity.this, PERMISSIONS,
                                        RC_PERMISSION_ALL);
                            }
                        }
                    })
                    .create().show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, PERMISSIONS,
                        RC_PERMISSION_ALL);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //mLocationPermissionGranted = false;
        // If request is cancelled, the result arrays are empty.
        if (requestCode == RC_PERMISSION_ALL) {
            if (permissions.length > 0 && /*permissions[0].equals(android.Manifest.permission.READ_EXTERNAL_STORAGE) &&*/ grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else if (grantResults.length > 0 &&/* permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && */grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
//                    mLocationPermissionGranted = true;
            } else if (/*grantResults.length > 0 &&*/ grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permissions denied the app will shut down shortly", Toast.LENGTH_LONG).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }
        }

        //      donot allow the onmap raedy proceed unless the permissions are granted and gps is on
//
    }


    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void checkIfUserIsCoonnectedToProducer() {
        this.mAlertDialog.show();
        //lter deal with query
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && ConsumerDashBoardActivity.this != null) {
                            boolean found = false;
                            outerLoop:
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {
                                        producerId = dataSnapshot.getChildren().iterator().next().getKey();//producer key
                                        found = true;
//                                        fetchReleventDataUsingKey(producerId);
                                        mAlertDialog.dismiss();
                                        break outerLoop;
                                    }
                                }
                            }
                            if (!found) {
                                mAlertDialog.dismiss();
                                upDateUiForNoProducerConnection();
                            }
                            // TODO: 7/14/2019  later change the query along with the change in data
                        } else {
                            mAlertDialog.dismiss();
                            upDateUiForNoProducerConnection();

                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void upDateUiForNoProducerConnection() {
        offdeliveryDaysButton.setEnabled(false);
    }

//    private void sendUserToRequestActivity() {
//
//        startActivity(new Intent(this, ConsumerRequestsActivity.class));
//    }
//
////    private void pouplateRelevantData() {
////
////        // FIXME: 8/25/2019 later the consumer id be already fetchedand saved in shared preference
////
////        //first fetch the producer id
////        //// TODO: 8/25/2019 iterating over al data is very bad but will be taken care of in future by makkin queries or prefetching the producer id one time
////        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
////                .addListenerForSingleValueEvent(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                        if (dataSnapshot.exists()) {
////
////                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
////
////                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
////                                    if (clientSnap.getKey().equals(myId)) {
////                                        producuerKey = producerSnap.getKey();//producer key
////
////                                        fetchReleventDataUsingKey(producuerKey);
////
////                                        break;
////                                    }
////                                }
////                            }
////
////                            // TODO: 7/14/2019  later change the query along with the change in data
////                        }
////
////                    }
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////
////                    }
////                });
////
////
////    }

//    private void fetchReleventDataUsingKey(final String producuerKey) {
//
//        //used for setting and getting clients own data fields
//        rootRef.child("clients")
//                .child(producuerKey)
//                .child(myId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//
//                            if (!dataSnapshot.child("live_data").child("at_home").exists()) {
//                                mIsAtHome = true;
//                                ConsumerFirebaseHelper.atHome(mIsAtHome, producuerKey);
//                            } else {
//                                mIsAtHome = dataSnapshot.child("live_data").child("at_home").getValue(Boolean.class);
//                                if (mIsAtHome) offdeliveryDaysButton.setText("I am not at Home");
//                                else offdeliveryDaysButton.setText("I am at Home");
//
//                            }
//
//                        } else {
//                            Toast.makeText(ConsumerDashBoardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                        }
//                        mAlertDialog.dismiss();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        mAlertDialog.dismiss();
//
//                    }
//                });
//
//
////        //to get the producer related infromation
////        rootRef.child("data").child(producuerKey)
////                .child("live_data").addValueEventListener(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                if (dataSnapshot.exists()) {
////                    //outer variables
////                    producerStatus = dataSnapshot.child("status").getValue(String.class);
////                    mCurrentMilkPrice = dataSnapshot.child("milk_price").getValue(String.class);
////                    if (producerStatus == null) {
////                    if (producerStatus == null) {
////                        Toast.makeText(ConsumerDashBoardActivity.this, "Producer status was null ", Toast.LENGTH_SHORT).show();
////                        producerStatus = "unknown";
////                        producerStatustextView.setText(producerStatus);
////                        return;
////                    }
////                    if (producerStatus.equals(getResources().getString(R.string.status_producer_inactive))) {
////                        producerStatustextView.setTextColor(Color.GRAY);
////                    } else if (producerStatus.equals(getResources().getString(R.string.status_producer_onduty))) {
////                        producerStatustextView.setTextColor(Color.GREEN);
////                    }
////                    producerStatustextView.setText(producerStatus);
////                    producerStatustextView.setText(producerStatus);
////                }
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError databaseError) {
////
////            }
////
////
////        });
//
//    }

    private void checkIfDeliveryLocationIsProvided() {
        rootRef.child("users").child(myId).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ConsumerDashBoardActivity.this != null) {
                    if (!dataSnapshot.exists()) { //location is not set
                        showDialogToSendToPickupLocationActivty();
                    } else {//location was set
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", dataSnapshot.child("lat").getValue(String.class));
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", dataSnapshot.child("lng").getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDialogToSendToPickupLocationActivty() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_warning_black_24dp);
        builder.setTitle("Set Delivery Location");
        builder.setMessage("the location for delivery of goods(milk) is not set. " +
                "Click proceed to specify it on Map");
        builder.setPositiveButton("proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendToPickLocationActivity();

            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ConsumerDashBoardActivity.this, "you will not be able to send request " +
                        "to Providers unless you set up the delivery Location", Toast.LENGTH_LONG).show();
            }
        });
        builder.create().show();
    }

    private void sendToPickLocationActivity() {
        Intent intent = new Intent(this, PickLocationMapsActivity.class);
        intent.putExtra(getResources().getString(R.string.from_which_activty), getResources().getString(R.string.from_consumer_dash_board));
        startActivityForResult(intent, RC_SET_DELIVERY_LOCATION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SET_DELIVERY_LOCATION) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra("lat", 0);
                double lng = data.getDoubleExtra("lng", 0);
                if (lat == 0 && lng == 0) {
                    Toast.makeText(this, "seems like something went wrong ", Toast.LENGTH_SHORT).show();

                } else {
                    sendDataToFirebase(lat, lng);
                }
            }

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendDataToFirebase(double lat, double lng) {
        HashMap<String, Object> locationMap = new HashMap<>();
        locationMap.put("lat", "" + lat);
        locationMap.put("lng", "" + lng);

        rootRef.child("users").child(myId).child("location").setValue(locationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", lat + "");
                            SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", lng + "");
                            Toast.makeText(ConsumerDashBoardActivity.this, "Delivery Location set1 Successfully", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(ConsumerDashBoardActivity.this, "unable to set Delivery Location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_con_dash_off_delivery_days:
                startActivity(new Intent(this, DaysOffActivity.class));

//                showNoMilkRecieveAlert(v);
                break;
            case R.id.bt_con_dash_show_summery:
                ClientSummeryFragment clientSummeryFragment = ClientSummeryFragment.newInstance(producerId);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.consumer_dash_board_fragment_container, clientSummeryFragment, SUMMERY_FRAG_TAG)
                        .commit();
                break;
            case R.id.bt_con_dash_my_providers:
                startActivity(new Intent(this, ConsumerRequestsActivity.class));
                break;
            case R.id.bt_con_dash_my_services:
                startActivity(new Intent(this, AcquiredGoodsActivity.class));
                break;
//            case R.id.bt_con_show_map:
//                Intent intent = new Intent(this, ConsumerMapActivity.class);
//                intent.putExtra("producer_id", producerId);
//                startActivity(intent);
//                break;
            case R.id.bt_con_dash_my_profile:
                Intent intent1 = new Intent(this, PersonalInfoActivity.class);
                startActivity(intent1);
                break;
            case R.id.bt_con_dash_notifications:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;

        }
    }


    private void showNoMilkRecieveAlert(View v) {

        String message = null;
        String title = null;
        if (mIsAtHome) {
            title = "Dont Want Milk at Home?? ";
            message = "are you sure that you dont want the Milkseller to be at home today";

        } else {
            title = "At home?";
            message = "Want milk at home ?";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mIsAtHome) {
                            ConsumerFirebaseHelper.atHome(false, producerId);
                            mIsAtHome = false;
                        } else {
                            ConsumerFirebaseHelper.atHome(true, producerId);
                            mIsAtHome = true;
                        }
                    }
                }).setNegativeButton("cancel", null).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.consumer_dash_board, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_consumer_logout:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    showLogoutDialogue();
                } else {
                    Toast.makeText(this, "ops ! something went wrong, you were too quick", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showLogoutDialogue() {
        //logout code
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("press logout to continue..")
                .setPositiveButton("logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessagingTokenAndLogout();
                    }
                }).setNegativeButton("cancel", null);
        builder.show();
    }

    private void deleteMessagingTokenAndLogout() {
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("messaging_token").setValue("null").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AuthUI.getInstance()
                        .signOut(ConsumerDashBoardActivity.this)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", null);
                                SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", null);
                                Toast.makeText(ConsumerDashBoardActivity.this, "logout successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ConsumerDashBoardActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ConsumerDashBoardActivity.this, "logout failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConsumerDashBoardActivity.this, "logout failed please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag(SUMMERY_FRAG_TAG) != null) {
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(SUMMERY_FRAG_TAG)).commit();
        } else {
            super.onBackPressed();
        }

    }
}
// TODO: 8/6/2019  put a braod cast receiver when GPs is turned on adn off and then trigger the location api
