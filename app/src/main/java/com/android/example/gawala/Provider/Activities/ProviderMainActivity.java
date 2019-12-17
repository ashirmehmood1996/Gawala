package com.android.example.gawala.Provider.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.example.gawala.Generel.Activities.LoginActivity;
import com.android.example.gawala.Generel.Activities.NotificationsActivity;
import com.android.example.gawala.Generel.Activities.PickLocationMapsActivity;
import com.android.example.gawala.Generel.Activities.ProfileActivity;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.android.example.gawala.Provider.Fragments.ProviderClientsFragment;
import com.android.example.gawala.R;
import com.android.example.gawala.Transporter.Fragments.ProducerSummeryFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProviderMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_PERMISSION_ALL = 1234;
    private static final int RC_SET_DELIVERY_LOCATION = 121;
    private static final int RC_LOCAION_ON = 122;
    private static final String SUMMERY_FRAGMENT_TAG = "FragSummery";
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION};


    private Button clientsButton, ridersButton, historyButton, servicesButton, notificationsButton, profileButton;
    private String myID;
    private String PROVIDER_CLIENT_REQUEST_FRAGMENT_TAG = "providerClientFragmentTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_main);
        getSupportActionBar().setTitle("Provider");
        initFields();
        attachListeners();

        if (!hasPermissions(this, PERMISSIONS)) {
            requestAllPermissions();
        } else {
            checkIfDeliveryLocationIsProvided();
        }
        UtilsMessaging.initFCM();

    }


    private void initFields() {
        //view related
        clientsButton = findViewById(R.id.bt_provider_main_clients);
        ridersButton = findViewById(R.id.bt_provider_main_riders);
        historyButton = findViewById(R.id.bt_provider_main_history);
        servicesButton = findViewById(R.id.bt_provider_main_services);
        notificationsButton = findViewById(R.id.bt_provider_main_notifications);
        profileButton = findViewById(R.id.bt_provider_main_profile);

        //data related
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void attachListeners() {
        clientsButton.setOnClickListener(this);
        ridersButton.setOnClickListener(this);
        historyButton.setOnClickListener(this);
        servicesButton.setOnClickListener(this);
        notificationsButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);
    }

    //permissions related start

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
                                ActivityCompat.requestPermissions(ProviderMainActivity.this, PERMISSIONS,
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

    //permissions related end


    //delivery location related start

    private void checkIfDeliveryLocationIsProvided() {
        rootRef.child("users").child(myID).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() && ProviderMainActivity.this != null) { //location is not set
                    showDialogToSendToPickupLocationActivty();
                } else {//location was set
                    SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", dataSnapshot.child("lat").getValue(String.class));
                    SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", dataSnapshot.child("lng").getValue(String.class));
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
        builder.setTitle("Set your Shop/Business Location");
        builder.setMessage("The location of your Business is not set. " +
                "Click proceed to specify it on Map");
        builder.setPositiveButton("proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendToPickLocationActivity();

            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ProviderMainActivity.this, "you will not be able to proivide Services " +
                        "to Consumers unless you specify your Business Location", Toast.LENGTH_LONG).show();
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

        rootRef.child("users").child(myID).child("location").setValue(locationMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", lat + "");
                        SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", lng + "");
                        Toast.makeText(getApplicationContext(), "Shop/Business Location set Successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "unable to set Shop/Business Location", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //delivery location related end


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.provider_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_provider_logout:
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
        rootRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("messaging_token").setValue("null").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AuthUI.getInstance()
                        .signOut(ProviderMainActivity.this)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", null);
                                SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", null);
                                SharedPreferenceUtil.clearAllPreferences(getApplicationContext());
                                Toast.makeText(ProviderMainActivity.this, "logout successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ProviderMainActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ProviderMainActivity.this, "logout failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProviderMainActivity.this, "logout failed please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_provider_main_clients:
                showClientsFragment();
                break;
            case R.id.bt_provider_main_riders:
                startActivity(new Intent(this, ProviderTransportersActivity.class));
                break;
            case R.id.bt_provider_main_history:
                showSummeryFragment();
                break;
            case R.id.bt_provider_main_services:
                startActivity(new Intent(this, ProducerServicesActivty.class));
                break;
            case R.id.bt_provider_main_notifications:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;
            case R.id.bt_provider_main_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }
    }

    private void showSummeryFragment() {


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ProducerSummeryFragment producerSummeryFragment = (ProducerSummeryFragment) getSupportFragmentManager().findFragmentByTag(SUMMERY_FRAGMENT_TAG);
        if (producerSummeryFragment != null) {
            fragmentTransaction.remove(producerSummeryFragment);
        }
        ProducerSummeryFragment dialogFragment = ProducerSummeryFragment.newInstance(myID, true);
//        dialogFragment.setCallback(this);
//        dialogFragment.setProducerSummeryModel(producerSummeryModel);
        dialogFragment.show(fragmentTransaction, SUMMERY_FRAGMENT_TAG);

    }

    private void showClientsFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ProviderClientsFragment providerClientsFragment =
                (ProviderClientsFragment) getSupportFragmentManager().findFragmentByTag(PROVIDER_CLIENT_REQUEST_FRAGMENT_TAG);
        if (providerClientsFragment != null) {
            fragmentTransaction.remove(providerClientsFragment);
        }
        ProviderClientsFragment dialogFragment = ProviderClientsFragment.getInstance();
//        dialogFragment.setCallback(this);
        dialogFragment.show(fragmentTransaction, PROVIDER_CLIENT_REQUEST_FRAGMENT_TAG);
    }
}
