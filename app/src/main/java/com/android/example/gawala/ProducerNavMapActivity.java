package com.android.example.gawala;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.example.gawala.Activities.LoginActivity;
import com.android.example.gawala.Activities.MainActivity;
import com.android.example.gawala.Activities.ProducerClientsRequestsActivity;
import com.android.example.gawala.Models.DistanceMatrixModel;
import com.android.example.gawala.Models.StopMarkerModel;
import com.android.example.gawala.Utils.HttpRequestHelper;
import com.android.example.gawala.Utils.UrlGenrator;
import com.android.example.gawala.directionhelpers.FetchURL;
import com.android.example.gawala.directionhelpers.TaskLoadedCallback;
import com.android.example.gawala.fragments.StopsListFragement;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Handler;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class ProducerNavMapActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, StopsListFragement.Callbacks, TaskLoadedCallback {

    private static final long FASTEST_INTERVAL = 10000;
    private static final long REQUEST_INTERVAL = 15000;
    private int selectedStopPos = 0;

    private static final String STOPS_LIST_FRAGMENT_TAG = "StopslistFragment";
    private GoogleMap mMap;
    private DrawerLayout drawer;
    private LocationResult mCurrentLocationResult;
    private ArrayList<StopMarkerModel> mStopMarkerModelArrayList;

    private FragmentManager mFragmentManager;
    private Marker myCurrentLocationMarker;
    private boolean isjournyActive;

    private View journyInfoContainer;
    private TextView distanceTextView, speedTextView, timeTextView;
    private Polyline mCurrentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_map);

        initFields();

    }

    private void initFields() {
        Toolbar toolbar = findViewById(R.id.tb_producer_nav);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
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

        mStopMarkerModelArrayList = new ArrayList<>();
        mFragmentManager = getSupportFragmentManager();

        journyInfoContainer = findViewById(R.id.ll_journy_info_container);
        distanceTextView = findViewById(R.id.tv_prod_distance);
        timeTextView = findViewById(R.id.tv_prod_time);
        speedTextView = findViewById(R.id.tv_prod_speed);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mFragmentManager.findFragmentByTag(STOPS_LIST_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(STOPS_LIST_FRAGMENT_TAG)).commit();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (item.getItemId() == R.id.nav_producer_map_logout) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProducerNavMapActivity.this, "logout successfull", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ProducerNavMapActivity.this, LoginActivity.class));
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
        } else if (item.getItemId() == R.id.nav_producer_map_start_riding) {
            isjournyActive = true;
            startJourney();
            journyInfoContainer.setVisibility(View.VISIBLE);
        } else if (item.getItemId() == R.id.nav_producer_map_abort_riding) {
            isjournyActive = false;
            journyInfoContainer.setVisibility(View.GONE);
        } else if (item.getItemId() == R.id.nav_producer_map_add_new_stop) {
            addCurrentLoadtionInFirebaseAsAStop();

        } else if (item.getItemId() == R.id.nav_producer_map_all_stops) {
            if (mFragmentManager.findFragmentByTag(STOPS_LIST_FRAGMENT_TAG) == null) {
                StopsListFragement stopListFragment = StopsListFragement.newInstance(mStopMarkerModelArrayList, "");
                stopListFragment.setCallBacks(this);
                mFragmentManager.beginTransaction()
                        .add(R.id.frame_prod_map_fragment_container,
                                stopListFragment
                                , STOPS_LIST_FRAGMENT_TAG)
                        .commit();
            }
        }
//         else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * this method is responsible for adding the active location to firebase and will be treated as a stop
     * this location will be used as a marker
     */
    private void addCurrentLoadtionInFirebaseAsAStop() {
        if (mCurrentLocationResult != null) {
            //// TODO: 7/30/2019  later this stp will be associated with client
            FirebaseDatabase.getInstance().getReference()
                    .child("stops").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .push().setValue(mCurrentLocationResult).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProducerNavMapActivity.this, "this location is successfully marked", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProducerNavMapActivity.this, "error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Toast.makeText(this, "please provide permissions first", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mMap.setMyLocationEnabled(true);

        animateUserLocation();

        mMap.getUiSettings().setZoomControlsEnabled(true);
        //   mMap.getUiSettings().setCompassEnabled(true);
        mMap.setPadding(0, 150, 0, 0); //todo this property may be pixed dependednt find a fix later


        //// TODO: 7/27/2019 need runtime permissions for accessing fine locations
        // TODO: 7/27/2019 ur on GPS if not active
        //// TODO: 7/27/2019 for the purpose of Gps use a broadcast reciever for making changes as desired
        //now i am able to ge the location now further take the next lessonss


        createLocationRequest();

        listenTomyLocation();

        loadStopMarkers();
        adDraglisternerToMAp();


    }

    private void animateUserLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

        } else {
            if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//                ActivityCompat.requestPermissions(this, new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                // Permissions ok, we get last location
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16.0f));
//                            Toast.makeText(MapsActivity.this, "FusedLocationProviderClient Last known Location and provider is " + location.getProvider() + "\n" + "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }

    }

    private void listenTomyLocation() {

        FirebaseDatabase.getInstance().getReference().child("locationUpdaes")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
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
                });
    }

    private void setMarker(double lat, double lng) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once

        LatLng location = new LatLng(lat, lng);

        if (myCurrentLocationMarker == null) {
            myCurrentLocationMarker = mMap.addMarker(new MarkerOptions().title("my position").position(location).draggable(true));
        } else {
            myCurrentLocationMarker.setPosition(location);
        }
//        for (Marker marker : mMarkers.values()) {
//            builder.include(marker.getPosition());
//        }

        //mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }


    //location related
    protected void createLocationRequest() {
        //this request is to chec the necessary settings
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(REQUEST_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(ProducerNavMapActivity.this, "Success", Toast.LENGTH_SHORT).show();
                requestLocationUpdates();

            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Toast.makeText(ProducerNavMapActivity.this, "resolvable failure", Toast.LENGTH_SHORT).show();
//               try {
//                   // Show the dialog by calling startResolutionForResult(),
//                   // and check the result in onActivityResult().
////                   ResolvableApiException resolvable = (ResolvableApiException) e;
////                   resolvable.startResolutionForResult(ProducerClientsRequestsActivity.this,
////                           REQUEST_CHECK_SETTINGS);
//               } catch (IntentSender.SendIntentException sendEx) {
//                   // Ignore the error.
//               }

                } else {
                    Toast.makeText(ProducerNavMapActivity.this, "non resolvable failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void requestLocationUpdates() {
        //this will cause the location to be uptoTime in a node in firebase but I dont want too much of that
        LocationRequest request = new LocationRequest();
        request.setInterval(REQUEST_INTERVAL);
        request.setFastestInterval(FASTEST_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        //final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "have permissions", Toast.LENGTH_SHORT).show();
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    mCurrentLocationResult = locationResult;
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
                    FirebaseDatabase.getInstance().getReference().child("locationUpdaes")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            /*.push()*/.setValue(locationResult).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
//                                Toast.makeText(ProducerNavMapActivity.this, "node updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProducerNavMapActivity.this, "error updating firebase", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }, null);// FIXME: 8/8/2019 detach the listener when the user logs out or activity is teminated
        } else {
            Toast.makeText(this, "no permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStopMarkers() {
        FirebaseDatabase.getInstance().getReference()
                .child("stops").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String id = dataSnapshot.getKey();
                        double lat = dataSnapshot.child("locations").child("0")
                                .child("latitude").getValue(Double.class);
                        double lng = dataSnapshot.child("locations").child("0")
                                .child("longitude").getValue(Double.class);
                        long timeStamp = dataSnapshot.child("locations").child("0")
                                .child("time").getValue(Long.class);

                        StopMarkerModel stopMarkerModel = new StopMarkerModel(id, lat, lng, timeStamp);
                        mStopMarkerModelArrayList.add(stopMarkerModel);
                        createNewMarker(stopMarkerModel);

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


    //this method is only valid if it is called for the last posiioned object of array list
    private void createNewMarker(StopMarkerModel stopMarkerModel) {
        String key = stopMarkerModel.getId();
        MarkerOptions markerOptions = new MarkerOptions();
        //added array list size to indicate the number of marker
        markerOptions.title("marker" + mStopMarkerModelArrayList.size() + ":" + key);
        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.marker_home));
        markerOptions.draggable(true);
        markerOptions.position(new LatLng(stopMarkerModel.getLatitude(),
                stopMarkerModel.getLongitude()));
        Marker marker = mMap.addMarker(markerOptions);
        mStopMarkerModelArrayList.get(mStopMarkerModelArrayList.size() - 1).setMarker(marker);
    }

    private void adDraglisternerToMAp() {
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                for (StopMarkerModel stopMarkerModel : mStopMarkerModelArrayList) {
                    if (stopMarkerModel.getMarker().equals(marker)) {
                        stopMarkerModel.setLatitude(marker.getPosition().latitude);
                        stopMarkerModel.setLongitude(marker.getPosition().longitude);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("latitude", marker.getPosition().latitude);
                        hashMap.put("longitude", marker.getPosition().longitude);
                        FirebaseDatabase.getInstance().getReference()
                                .child("stops").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(stopMarkerModel.getId())
                                .child("locations").child("0")
                                .updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProducerNavMapActivity.this, "marker is placed ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    //copied from stack overflow lean later
    //https://stackoverflow.com/a/45564994/6039129
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onStopMarkerItemClick(int position) {
        selectedStopPos = position;
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(STOPS_LIST_FRAGMENT_TAG)).commit();
        Double lat = mStopMarkerModelArrayList.get(position).getLatitude();
        Double lng = mStopMarkerModelArrayList.get(position).getLongitude();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));

    }


    // stop to stop navigation module
    //logic take currunt location and destibaton as first element of stop makers array
    //calculate the distance between them
    // and time  and do it in a constant intrvals

    private void startJourney() {
        // TODO: 8/15/2019 this link will help you tomake a polyline as soon as the start journy is callled https://www.youtube.com/watch?v=wRDLjUK8nyU
        //  I assume that drawin gthe poliline for he first time will be use distance matrix for further calculations
        //  for now only deal with one stop.
        if (mCurrentLocationResult == null) {
            Toast.makeText(this, "cant get your location ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mStopMarkerModelArrayList.isEmpty()) {
            Toast.makeText(this, "there are no stops to reach to.. ", Toast.LENGTH_SHORT).show();
            return;
        }
        final LatLng currentLocation = new LatLng(mCurrentLocationResult.getLocations().get(0).getLatitude(), mCurrentLocationResult.getLocations().get(0).getLongitude());
        final LatLng stop1Location = new LatLng(mStopMarkerModelArrayList.get(0).getLatitude(), mStopMarkerModelArrayList.get(0).getLongitude());
// TODO: 8/15/2019 here i am
        new FetchURL(ProducerNavMapActivity.this).execute(getDirectionApiUrl(currentLocation, stop1Location), "driving");

        String url = UrlGenrator.generateDistanceMatrixUrl(currentLocation, stop1Location, getResources().getString(R.string.distance_matrix_api_key));
        DistanceMatrixAsyncTask distanceMatrixAsyncTask = new DistanceMatrixAsyncTask() {
            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    Toast.makeText(ProducerNavMapActivity.this, "response was null", Toast.LENGTH_LONG).show();
                } else {
                    System.out.println("response :" + s);
                    Toast.makeText(ProducerNavMapActivity.this, "response is recieved", Toast.LENGTH_LONG).show();
                    DistanceMatrixModel distanceMatrixModel = HttpRequestHelper.parseDistanceMatrixJson(s);


                    distanceTextView.setText(distanceMatrixModel.getDistance()/*distanceArray[0] + " meters"*/);
                    timeTextView.setText(distanceMatrixModel.getDuration());
                    speedTextView.setText(mCurrentLocationResult.getLocations().get(0).getSpeed() + " m/sec");

                    if (distanceMatrixModel.getDurationLong() <= 45) {
                        showNotificationForConsumer(distanceMatrixModel.getDurationLong());
                    }


                    Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isjournyActive) {
                                startJourney();
                            } else {
                                if (mCurrentPolyline != null) {
                                    mCurrentPolyline.remove();
                                    mCurrentPolyline = null;
                                }
                                Toast.makeText(ProducerNavMapActivity.this, "journey aborted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, REQUEST_INTERVAL);
                }

                super.onPostExecute(s);
            }
        };
        distanceMatrixAsyncTask.execute(url);
    }

    // TODO: 8/15/2019 create this one on your own and place in httphelper util class and add direction mode later if needed
    private String getDirectionApiUrl(LatLng origin, LatLng dest/*, String directionMode*/) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        //String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest /*+ "&" + mode*/;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getResources().getString(R.string.directions_api);
        return url;
    }

    private void showNotificationForConsumer(float timeRemaining) {
        // TODO: 8/4/2019  for now generating it in my own app later a node will be updated to notify the consumer usogn cloud fucntions further processing will be done

        //creating an intent and passing it through a pending intent which will be called when notification is clicked
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(//we can also call getService or broadcast reciever etc
                this, //context
                0, //id for pending intent if we want to cancel it later
                activityIntent, //intent to be executed by the notification
                0);
        //creating a notification
        NotificationCompat.Builder downloadNotificationBuilder;
        downloadNotificationBuilder = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_add_location_black_24dp)
                .setContentTitle("Alert Alert")
                .setContentText("your milk is about to arrive in " + timeRemaining + "seconds")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(11, downloadNotificationBuilder.build());

    }

    @Override
    public void onTaskDone(Object... values) {
        if (mCurrentPolyline != null)
            mCurrentPolyline.remove();
        mCurrentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

    }


    private class DistanceMatrixAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(ProducerNavMapActivity.this, "requesting json data", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String requestUrl = strings[0];
            return HttpRequestHelper.requestJsonData(requestUrl);
        }

    }

}


// TODO: 8/3/2019 now  show the list of stops that are added
//  shift the functionalities in places and polish the app
//  should not take long.
//  the markers should be prioritised       ///later


//all things below are only appilcable when I will activate my billing account

//Note :- you are in the middle of something . it seems like there is a way to have the distance and time required
//which will mark the begining of end of this project . you be have o run some precise tests on the code that comes available online
//may be you can try to find the distance and time finding tutorila on the (medium) or may be we only ned distance the time will be easys
//juts read a numbe rof projects and try to start implementing the latest and best explained
//how can I get the time after a little interval the amount of Time that will require to reach the destination
//may be we can ask for the distance and time again and again after time to time and will increase the frecuecy as we reach closer to target

//what to do with the billing  cant find hope // will do the billing account on monday