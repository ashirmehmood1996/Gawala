package com.android.example.gawala;

import android.Manifest;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.android.example.gawala.Activities.LoginActivity;
import com.android.example.gawala.Activities.ProducerClientsRequestsActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Toast;

public class NavProducerMapActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_map);

        initFields();

    }

    private void initFields() {
        Toolbar toolbar = findViewById(R.id.tb_producer_nav);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //   mMap.getUiSettings().setCompassEnabled(true);
        mMap.setPadding(0, 150, 0, 0); //todo this property may be pixed dependednt find a fix later

        FirebaseDatabase.getInstance().getReference().child("locationUpdaes")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        double lat = dataSnapshot.child("locations").child("0")
                                .child("latitude").getValue(Double.class);
                        double lng = dataSnapshot.child("locations").child("0")
                                .child("longitude").getValue(Double.class);
                        setMarker(lat, lng);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setMarker(double lat, double lng) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once

        LatLng location = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().title("new marker").position(location).draggable(true));
//        for (Marker marker : mMarkers.values()) {
//            builder.include(marker.getPosition());
//        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.producer_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.nav_producer_map_logout) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(NavProducerMapActivity.this, "logout successfull", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(NavProducerMapActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "already logged out", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.nav_producer_map_share_id) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Service.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "id copied to clipboard", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.nav_producer_map_requests) {
            startActivity(new Intent(this, ProducerClientsRequestsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


}

// TODO: 7/28/2019 now
//  add nav bar //today 29/07               ///done
//  add an option to add the starting point or updating it   //today 29/07
//  add an option to add a client stop and remove client stop //today 29/07
//  add an option to priorities the stops //today 29/07
//  think furtheron how to make the producer navigate from stop to stop //take a start today 29/07
//  first the user will have to press button on each stop to tell the app that its client or it has arrieved the client and it shoud be accurate with in few meters because we can not have same location coordinates of the stop to be able to know that the stop has arrived.


