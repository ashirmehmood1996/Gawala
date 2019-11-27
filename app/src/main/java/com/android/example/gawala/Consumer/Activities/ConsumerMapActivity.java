package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Property;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.example.gawala.Producer.Interfaces.LatLngInterpolator;
import com.android.example.gawala.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConsumerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int RC_PERMISSION_ALL = 1212;
    private GoogleMap mMap;
    private Marker mProducerLocationMarker;
    private String producerId;
    private String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,//NOTE when we make it read external storage we get the permissions but may be there is a bug that does make errors or permissions denied but by asking write permissions we get passed by that bug
            android.Manifest.permission.ACCESS_FINE_LOCATION};
    private int RC_LOCAION_ON = 12122;
    private ValueEventListener mLocationListener;
    private DatabaseReference mLocationUpdateNodeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!hasPermissions(this, PERMISSIONS)) {
            requestAllPermissions();
        } else if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlerDialogForGPS();
        } else {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            initfields();
        }

    }

    private void initfields() {
        producerId = getIntent().getStringExtra("producer_id");
        mLocationUpdateNodeRef = FirebaseDatabase.getInstance().getReference().child("locationUpdates")
                .child(producerId);
        mLocationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double lat = dataSnapshot.child("locations").child("0")
                            .child("latitude").getValue(Double.class);
                    double lng = dataSnapshot.child("locations").child("0")
                            .child("longitude").getValue(Double.class);
                    setMarker(lat, lng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
                                ActivityCompat.requestPermissions(ConsumerMapActivity.this, PERMISSIONS,
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

    private void showAlerDialogForGPS() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Enable Location !")
                .setMessage("Location must be enabled in settings in order to use this app." +
                        "Want to enable Location?")
                .setCancelable(false)
                .setPositiveButton("Yes, Go-to settings", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), RC_LOCAION_ON);
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Toast.makeText(ConsumerMapActivity.this, "location is not enabled app will shut down shortly", Toast.LENGTH_SHORT).show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms
                                finish();
                            }
                        }, 2000);
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_LOCAION_ON) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "location is not enabled", Toast.LENGTH_SHORT).show();
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    //Do something after 100ms
                    finish();
                }, 2000);
            } else {
                recreate();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        listenToProducerLocation();

    }


    private void listenToProducerLocation() {//later make samaller and speciific data calls
        mLocationUpdateNodeRef.addValueEventListener(mLocationListener);
    }

    private void setMarker(double lat, double lng) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once

        LatLng latLng = new LatLng(lat, lng);

        if (mProducerLocationMarker == null) {
            mProducerLocationMarker = mMap.addMarker(new MarkerOptions().title("Producer").position(latLng).draggable(true));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        } else {
//            mProducerLocationMarker.setPosition(location);
            animateMarkerToICS(mProducerLocationMarker, latLng, new LatLngInterpolator.LinearFixed()/*, currentChild.getChildChanginInfoModel().isSelected()*/);
        }
//        for (Marker marker : mMarkers.values()) {
//            builder.include(marker.getPosition());
//        }

        //mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }

    //code borrowed from google sample codes
    //https://gist.github.com/broady/6314689
    private void animateMarkerToICS(Marker marker, final LatLng finalPosition,
                                    final LatLngInterpolator latLngInterpolator/*, final boolean isSelected*/) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(300);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) { //to animate camera with the motion of child

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(finalPosition, 16.0f));

                animator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animator.removeAllListeners();

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mLocationUpdateNodeRef.removeEventListener(mLocationListener);
        super.onDestroy();
    }


}
