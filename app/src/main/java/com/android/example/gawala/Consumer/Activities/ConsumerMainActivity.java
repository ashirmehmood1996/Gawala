package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Consumer.fragments.ClientsSettingsFragment;
import com.android.example.gawala.Generel.Activities.LoginActivity;
import com.android.example.gawala.Generel.Activities.NotificationsActivity;
import com.android.example.gawala.Generel.Activities.ProfileActivity;
import com.android.example.gawala.Generel.Activities.PickLocationMapsActivity;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.R;
import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.android.example.gawala.Consumer.fragments.ClientSummeryFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ConsumerMainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String CLIENT_SUMMERY_FRAGMENT_TAG = "ClientsummeryFragment";
    private static final String TAG_SETTINGS_FRAGMENT = "settingFragment";
    private Button manageVacationsButton, showSummeryButton, myProvidesButton,
            myServicesButton, /*showMapButton,*/
            myProfilebutton, notificationsButton;

    //firbase related
    private String myId;

    private HashMap<String, String> providerIdNameMap;//provider key/ name
//    private ArrayList<String> providerIdArrayList;
//    private String providerId;
    //    private String producuerKey;
//    private String producerStatus;
//    private String mCurrentMilkPrice;

    private AlertDialog mProgressDialog;
    private int RC_SET_DELIVERY_LOCATION = 122;

    private final int RC_PERMISSION_ALL = 100;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_main);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateViewsIn();
        }

    }


    private void initFilds() {
        manageVacationsButton = findViewById(R.id.bt_con_dash_off_delivery_days);
        showSummeryButton = findViewById(R.id.bt_con_dash_show_summery);
        myProvidesButton = findViewById(R.id.bt_con_dash_my_providers);
        myServicesButton = findViewById(R.id.bt_con_dash_my_services);
//        showMapButton = findViewById(R.id.bt_con_show_map);
        myProfilebutton = findViewById(R.id.bt_con_dash_my_profile);
        notificationsButton = findViewById(R.id.bt_con_dash_notifications);


//        providerIdArrayList = new ArrayList<>();
        providerIdNameMap = new HashMap<>();
        //date related
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initializeDialog();
    }

    private void attachListeners() {
        manageVacationsButton.setOnClickListener(this);
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
                                ActivityCompat.requestPermissions(ConsumerMainActivity.this, PERMISSIONS,
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
        this.mProgressDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void checkIfUserIsCoonnectedToProducer() {
        this.mProgressDialog.show();


        rootRef.child("connected_producers")
                .child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (ConsumerMainActivity.this != null) {
                    mProgressDialog.dismiss();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot providerSnap : dataSnapshot.getChildren()) {
                            String providerId = providerSnap.getKey();
                            String providerName = providerSnap.child("name").getValue(String.class);
//                            providerIdArrayList.add(providerId);
                            providerIdNameMap.put(providerId, providerName);
                        }

                    } else {
                        //do something if needed to e.g. show a banner that indicated that [please connect o a producer
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        //lter deal with query
//        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists() && ConsumerMainActivity.this != null) {
////                            boolean found = false;
////                            outerLoop:
//                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
//                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
//                                    if (clientSnap.getKey().equals(myId)) {
//                                        String providerId = dataSnapshot.getChildren().iterator().next().getKey();//producer key
////                                        found = true;
//                                        providerIdArrayList.add(providerId);
////                                        fetchReleventDataUsingKey(providerId);
////                                        break outerLoop;
//                                    }
//                                }
//                            }
////                            if (!found) {
//                            mProgressDialog.dismiss();
////                                upDateUiForNoProducerConnection();
////                            }
//                            // TO DO: 7/14/2019  later change the query along with the change in data
//                        } else {
//                            mProgressDialog.dismiss();
////                            upDateUiForNoProducerConnection();
//
//                        }
//                    }
//
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//                });
    }

//    private void upDateUiForNoProducerConnection() {
//        manageVacationsButton.setEnabled(false);
//    }

//    private void sendUserToRequestActivity() {
//
//        startActivity(new Intent(this, ConsumerRequestsActivity.class));
//    }
//
////    private void pouplateRelevantData() {
////
////        // FIX ME: 8/25/2019 later the consumer id be already fetchedand saved in shared preference
////
////        //first fetch the producer id
////        //// TO DO: 8/25/2019 iterating over al data is very bad but will be taken care of in future by makkin queries or prefetching the producer id one time
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
////                            // TO DO: 7/14/2019  later change the query along with the change in data
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
//                                if (mIsAtHome) manageVacationsButton.setText("I am not at Home");
//                                else manageVacationsButton.setText("I am at Home");
//
//                            }
//
//                        } else {
//                            Toast.makeText(ConsumerMainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                        }
//                        mProgressDialog.dismiss();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        mProgressDialog.dismiss();
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
////                        Toast.makeText(ConsumerMainActivity.this, "Producer status was null ", Toast.LENGTH_SHORT).show();
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
                if (ConsumerMainActivity.this != null) {
                    if (!dataSnapshot.exists()) { //location is not set
                        showDialogToSendToPickupLocationActivty();
                    } else {//location was set
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", dataSnapshot.child("lat").getValue(String.class));
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", dataSnapshot.child("lng").getValue(String.class));
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "address", dataSnapshot.child("address").getValue(String.class));
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
                Toast.makeText(ConsumerMainActivity.this, "you will not be able to send request " +
                        "to Providers unless you set up fthe delivery Location", Toast.LENGTH_LONG).show();
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
                String address = data.getStringExtra("address");
                if (lat == 0 && lng == 0) {
                    Toast.makeText(this, "seems like something went wrong ", Toast.LENGTH_SHORT).show();
                } else {
                    sendDataToFirebase(lat, lng, address);
                }
            }

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendDataToFirebase(double lat, double lng, String address) {
        HashMap<String, Object> locationMap = new HashMap<>();
        locationMap.put("lat", "" + lat);
        locationMap.put("lng", "" + lng);
        locationMap.put("address", address);

        rootRef.child("users").child(myId).child("location").setValue(locationMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", lat + "");
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", lng + "");
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "address", address);
                        Toast.makeText(ConsumerMainActivity.this, "Delivery Location set Successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ConsumerMainActivity.this, "unable to set Delivery Location", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_con_dash_off_delivery_days:
                startActivity(new Intent(this, ManageVacationsActivity.class));

//                showNoMilkRecieveAlert(v);
                break;
            case R.id.bt_con_dash_show_summery:
                if (providerIdNameMap.isEmpty()) {
                    Toast.makeText(this, "please add producers first", Toast.LENGTH_SHORT).show();
                    return;
                }
                showFragmentToPickSummeryForEachProvider();
                break;
            case R.id.bt_con_dash_my_providers:
                startActivity(new Intent(this, ConsumerRequestsActivity.class));
                break;
            case R.id.bt_con_dash_my_services:
                startActivity(new Intent(this, AcquiredGoodsActivity.class));
                break;
//            case R.id.bt_con_show_map:
//                Intent intent = new Intent(this, ConsumerMapActivity.class);
//                intent.putExtra("producer_id", providerId);
//                startActivity(intent);
//                break;
            case R.id.bt_con_dash_my_profile:
                Intent intent1 = new Intent(this, ProfileActivity.class);
//                intent1.putStringArrayListExtra(ProfileActivity.PROVIDER_ID_ARRAY, providerIdArrayList);
                startActivity(intent1);
                break;
            case R.id.bt_con_dash_notifications:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;

        }
    }

    private void showFragmentToPickSummeryForEachProvider() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Provider ");
        builder.setNegativeButton("cancel", null);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(16,16,16,16);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.dialogtheme; //style id


        for (Map.Entry<String, String> providerMapEntry : providerIdNameMap.entrySet()) {
            RelativeLayout producerLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.li_producers, null);
            ((TextView) producerLayout.findViewById(R.id.tv_li_prod_name)).setText(providerMapEntry.getValue());
            producerLayout.findViewById(R.id.ll_li_prod_container).setOnClickListener(v -> {
                startSummeryFragment(providerMapEntry.getKey());
                alertDialog.cancel();
            });
            linearLayout.addView(producerLayout);
        }
        alertDialog.show();

    }

    private void startSummeryFragment(String providerId) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ClientSummeryFragment clientSummeryFragment =
                (ClientSummeryFragment) getSupportFragmentManager().findFragmentByTag(CLIENT_SUMMERY_FRAGMENT_TAG);
        if (clientSummeryFragment != null) {
            fragmentTransaction.remove(clientSummeryFragment);
        }
        ClientSummeryFragment dialogFragment = ClientSummeryFragment.newInstance(providerId);
//        dialogFragment.setCallBacks(this);
        dialogFragment.show(fragmentTransaction, CLIENT_SUMMERY_FRAGMENT_TAG);
    }


/*    private void showNoMilkRecieveAlert(View v) {

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
                .setPositiveButton("Yes", (dialog, which) -> {

                    if (mIsAtHome) {
                        ConsumerFirebaseHelper.atHome(false, providerId);
                        mIsAtHome = false;
                    } else {
                        ConsumerFirebaseHelper.atHome(true, providerId);
                        mIsAtHome = true;
                    }
                }).setNegativeButton("cancel", null).show();


    }*/

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
            case R.id.nav_consumer_settings:
                startSettingsFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void startSettingsFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ClientsSettingsFragment clientsSettingsFragment = (ClientsSettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS_FRAGMENT);
        if (clientsSettingsFragment != null) {
            fragmentTransaction.remove(clientsSettingsFragment);
        }
        clientsSettingsFragment = ClientsSettingsFragment.getInstance();
        clientsSettingsFragment.show(fragmentTransaction, TAG_SETTINGS_FRAGMENT);
    }

    private void showLogoutDialogue() {
        //logout code
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        rootRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("messaging_token").setValue("null").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AuthUI.getInstance()
                        .signOut(ConsumerMainActivity.this)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                SharedPreferenceUtil.clearAllPreferences(getApplicationContext());
                                Toast.makeText(ConsumerMainActivity.this, "logout successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ConsumerMainActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ConsumerMainActivity.this, "logout failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConsumerMainActivity.this, "logout failed please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //code borrowed from google must make understanding of it later
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void animateViewsIn() {
        // setup random initial state
        ViewGroup root = findViewById(R.id.ll_consumer_main_container);
        float maxWidthOffset = 2f * getResources().getDisplayMetrics().widthPixels;
        float maxHeightOffset = 2f * getResources().getDisplayMetrics().heightPixels;
        Interpolator interpolator =
                AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);
        Random random = new Random();
        int count = root.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = root.getChildAt(i);
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.85f);
            float xOffset = random.nextFloat() * maxWidthOffset;
            if (random.nextBoolean()) {
                xOffset *= -1;
            }
            view.setTranslationX(xOffset);
            float yOffset = random.nextFloat() * maxHeightOffset;
            if (random.nextBoolean()) {
                yOffset *= -1;
            }
            view.setTranslationY(yOffset);

            // now animate them back into their natural position
            view.animate()
                    .translationY(0f)
                    .translationX(0f)
                    .alpha(1f)
                    .setInterpolator(interpolator)
                    .setDuration(1000)
                    .start();
        }
    }

}

