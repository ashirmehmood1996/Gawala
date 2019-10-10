package com.android.example.gawala.Producer.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.example.gawala.Generel.Activities.LoginActivity;
import com.android.example.gawala.Generel.Activities.MainActivity;
import com.android.example.gawala.Generel.App;
import com.android.example.gawala.Producer.Adapters.AciveRideStopsAdaper;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.Producer.Fragments.ProducerClientsRequestsFragment;
import com.android.example.gawala.Producer.Interfaces.LatLngInterpolator;
import com.android.example.gawala.Producer.Models.DistanceMatrixModel;
import com.android.example.gawala.Producer.Utils.ProducerFirebaseHelper;
import com.android.example.gawala.Producer.Utils.HttpRequestHelper;
import com.android.example.gawala.Producer.Utils.UrlGenrator;
import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.android.example.gawala.Producer.directionhelpers.FetchURL;
import com.android.example.gawala.Producer.directionhelpers.TaskLoadedCallback;
import com.android.example.gawala.Producer.Fragments.ProducerDashBoardFragment;
import com.android.example.gawala.Producer.Fragments.ProducerSummeryFragment;
import com.android.example.gawala.R;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Handler;
import android.util.Property;
import android.view.MenuItem;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class ProducerNavMapActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        ProducerDashBoardFragment.Callbacks, TaskLoadedCallback,
        ProducerClientsRequestsFragment.CallBacks {


    private final int RC_PERMISSION_ALL = 100;
    private final int RC_LOCAION_ON = 101;
    private final String PRODUCER_DASHBOARD_FRAGMENT_TAG = "ProducerDashBoardFragment";
    private final String SUMMERY_FRAGMENT_TAG = "SummeryProducerTag";
    private String PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG = "ProducerClientRequestFragment";
    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    private static final long FASTEST_INTERVAL = 10000;
    private static final long REQUEST_INTERVAL = 15000;
    private int selectedStopPos = 0;


    private GoogleMap mMap;
    private DrawerLayout drawer;
    private LocationResult mCurrentLocationResult;
    private ArrayList<ConsumerModel> mConsumerModelArrayList;

    private FragmentManager mFragmentManager;
    private Marker myCurrentLocationMarker;
    private boolean isjournyActive;


    private Polyline mCurrentPolyline;
    private LocationManager locationManager;
    private Polyline mFullRoutePolyline;
    private boolean shouldDrawfullRoute = true;
    public static int activeStopPosition = 0;

    private Button deliveredToCurrentStopButton;
    //    private View journyInfoContainer;
    private TextView distanceTextView, speedTextView, timeTextView;
//    private Button abortJournyButton;

    //bottom sheet related
    private LinearLayout bottomSheetLinearLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView mActiveRideSopsRecyclerView;
    //    private ArrayList<ActiveRideStopsModel> mActiveRideStopsModelArrayList;
    private AciveRideStopsAdaper mAciveRideStopsAdapter;
    private float mTotalMilkDemand;
    private ImageButton toggleImageButton;
    private int mMilkPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_map);

        initFields();
        attachListeners();
        if (!hasPermissions(this, PERMISSIONS)) {
            requestAllPermissions();
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        UtilsMessaging.initFCM();


        //// FIXME: 8/25/2019 for now setting the rate initially according to market later it will be changed
        initUserDataIfFirstTime();

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
                                ActivityCompat.requestPermissions(ProducerNavMapActivity.this, PERMISSIONS,
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

    private void buildAlertMessageNoGps() {
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
                        Toast.makeText(ProducerNavMapActivity.this, "location is not enabled app will shut down shortly", Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
                Toast.makeText(this, "location is not enabled app will shut down shortly", Toast.LENGTH_SHORT).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        finish();
                    }
                }, 2000);
            } else {
                recreate();
            }

//                }
        }


    }

    private void initFields() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Toolbar toolbar = findViewById(R.id.tb_producer_nav);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Map");
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mConsumerModelArrayList = new ArrayList<>();
        mFragmentManager = getSupportFragmentManager();

//        journyInfoContainer = findViewById(R.id.ll_journy_info_container);
        bottomSheetLinearLayout = findViewById(R.id.ll_bottom_sheet_active_riding_info_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLinearLayout);
        initBottomSheet();


    }

    private void initBottomSheet() {


        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //recyclerview related
        mActiveRideSopsRecyclerView = findViewById(R.id.rv_bottom_sheet_active_riding);
//        mActiveRideStopsModelArrayList=new ArrayList<>();
        mAciveRideStopsAdapter = new AciveRideStopsAdaper(this, mConsumerModelArrayList);
        mActiveRideSopsRecyclerView.setAdapter(mAciveRideStopsAdapter);

//        mAciveRideStopsAdapter


        toggleImageButton = findViewById(R.id.bt_bottom_sheet_toggle);
        distanceTextView = findViewById(R.id.tv_bottom_sheet_prod_distance);
        timeTextView = findViewById(R.id.tv_bottom_sheet_prod_time);
        speedTextView = findViewById(R.id.tv_bottom_sheet_prod_speed);
//        abortJournyButton = findViewById(R.id.bt_prod_abort_journy);
        deliveredToCurrentStopButton = findViewById(R.id.bt_prod_delivered_to_current_stop);
        setBottomSheetCallBacks();

    }

    private void setBottomSheetCallBacks() {

        //bottom sheet state change listener
        //we are changing button text when sheet changed state
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {

                    case BottomSheetBehavior.STATE_HIDDEN:
                        mMap.setPadding(0, 150, 0, 0);
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:

                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mMap.setPadding(0, 150, 0, bottomSheetBehavior.getPeekHeight());
                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:

                        break;

                    case BottomSheetBehavior.STATE_SETTLING:

                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    private void attachListeners() {
//        abortJournyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                abortJourny();
//            }
//        });

        deliveredToCurrentStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConsumerModelArrayList.get(activeStopPosition).setDelivered(true);
                if (activeStopPosition >= mConsumerModelArrayList.size() - 1) { //this means that all stops are done
                    abortJourny();
                    Toast.makeText(ProducerNavMapActivity.this, "all stops done, Now finishing Ride....", Toast.LENGTH_SHORT).show();
                } else {
                    activeStopPosition++;
                    mAciveRideStopsAdapter.notifyDataSetChanged();
                    Toast.makeText(ProducerNavMapActivity.this, "successfully marked as delivered, Now going gor next stop", Toast.LENGTH_SHORT).show();
                }
            }
        });
        toggleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                else bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


            }
        });
    }

    private void abortJourny() {
        if (!isjournyActive) {
            Snackbar.make(drawer, "no ride was active", Snackbar.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(drawer, "processing...", Snackbar.LENGTH_SHORT).show();
        addSessionToDatabase();
        isjournyActive = false;
        activeStopPosition = 0;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void addSessionToDatabase() {
        DatabaseReference permanentDataRef = FirebaseDatabase.getInstance().getReference()
                .child("data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("permanent_data").push();

        HashMap<String, Object> mainMap = new HashMap<>();
        HashMap<String, Object> clientsMap = new HashMap<>();
        for (ConsumerModel consumerModel : mConsumerModelArrayList) {
            String id = consumerModel.getId();
//            String time_stamp=//fixme include later  //the time at which this stop was visited
            String name = consumerModel.getName();
            float amounOfMilk = consumerModel.getAmountOfMilk();
            boolean status = consumerModel.isDelivered();


            HashMap<String, Object> clientDatamap = new HashMap<>();

            clientDatamap.put("name", name);
            clientDatamap.put("milk_amount", amounOfMilk);
            clientDatamap.put("status", status);

            clientsMap.put(id, clientDatamap);
        }
        mainMap.put("clients", clientsMap);
        mainMap.put("time_stamp", Calendar.getInstance().getTimeInMillis());//the time at ehich this session was put to an end
        mainMap.put("milk_price", mMilkPrice); //adding price just because it can change from day to day
        permanentDataRef.setValue(mainMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ProducerNavMapActivity.this, "session successfully uploaded to cloud database", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initUserDataIfFirstTime() {
        FirebaseDatabase.getInstance().getReference().child("data")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("milk_price").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    ProducerFirebaseHelper.updateRate("100");
                    mMilkPrice = 120;
                } else {
                    mMilkPrice = Integer.parseInt(dataSnapshot.getValue(String.class));

                }


                ProducerFirebaseHelper.updateStatus(getResources().getString(R.string.status_producer_inactive));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG)).commit();
            getSupportActionBar().setTitle("Map");
        } else if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG)).commit();
            getSupportActionBar().setTitle("Map");
        } else if (mFragmentManager.findFragmentByTag(SUMMERY_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(SUMMERY_FRAGMENT_TAG)).commit();
            getSupportActionBar().setTitle("Map");
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_producer_map_logout:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    showLogoutDialogue();
                } else {
                    Toast.makeText(this, "already logged out", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_producer_map_share_id:
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Service.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                clipboardManager.setPrimaryClip(clipData);
                Snackbar.make(drawer, "id copied to clipboard", Snackbar.LENGTH_LONG);
                Toast.makeText(this, "id copied to clipboard", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_producer_map_clients:


                if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) == null) {
                    ProducerClientsRequestsFragment producerClientsRequestsFragment = ProducerClientsRequestsFragment.geInstance();
                    producerClientsRequestsFragment.setCallBacks(this);

                    mFragmentManager.beginTransaction()
                            .replace(R.id.frame_prod_map_fragment_container,
                                    producerClientsRequestsFragment
                                    , PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG)
                            .commit();
                }
                break;
            case R.id.nav_producer_map_dashboard:
                if (mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG) == null) {
                    ProducerDashBoardFragment producerDashBoardFragment = ProducerDashBoardFragment.geInstance(mConsumerModelArrayList, "");
                    producerDashBoardFragment.setCallBacks(this);

                    mFragmentManager.beginTransaction()
                            .replace(R.id.frame_prod_map_fragment_container,
                                    producerDashBoardFragment
                                    , PRODUCER_DASHBOARD_FRAGMENT_TAG)
                            .commit();
                }
                break;
            case R.id.nav_producer_map_rides_summery:
                if (mFragmentManager.findFragmentByTag(SUMMERY_FRAGMENT_TAG) == null) {
                    ProducerSummeryFragment producerSummeryFragment = ProducerSummeryFragment.newInstance();


                    mFragmentManager.beginTransaction()
                            .replace(R.id.frame_prod_map_fragment_container,
                                    producerSummeryFragment
                                    , SUMMERY_FRAGMENT_TAG)
                            .commit();
                }
                break;
            case R.id.nav_producer_my_services:
                startActivity(new Intent(this, ProducerServicesActivty.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialogue() {
        //logout code
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ProducerNavMapActivity.this);
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
                        .signOut(ProducerNavMapActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProducerNavMapActivity.this, "logout successfull", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ProducerNavMapActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProducerNavMapActivity.this, "logout failed please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

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


        createLocationRequest();

        listenTomyLocation();

        loadAllConsumers(); // may be latewe we shif the consumers to another maps activity that also contains a map fragemnt to set the stops location
//        loadAllConsumersForTodaysJourney();
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
//            myCurrentLocationMarker.setPosition(location);
            animateMarkerToICS(myCurrentLocationMarker, location, new LatLngInterpolator.LinearFixed()/*, currentChild.getChildChanginInfoModel().isSelected()*/);
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
                    // FIXME: 9/20/2019 later use this reolution code experimentally to resolve the issue
//               try {
//                   // Show the dialog by calling startResolutionForResult(),
//                   // and check the result in onActivityResult().
////                   ResolvableApiException resolvable = (ResolvableApiException) e;
////                   resolvable.startResolutionForResult(ProducerClientsRequestsFragment.this,
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
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())// FIXME: 9/8/2019 fix float to string cast exception
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

    private void loadAllConsumers() {
        FirebaseDatabase.getInstance().getReference()
                .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())//prodcuer id
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String id = dataSnapshot.getKey();
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String number = dataSnapshot.child("number").getValue(String.class);
                        String timeStamp = dataSnapshot.child("time_stamp").getValue(String.class);
                        float milkDemand = 0.0f;
                        if (dataSnapshot.child("live_data").child("milk_demand").exists()) {
                            milkDemand = Float.parseFloat(dataSnapshot.child("live_data").child("milk_demand").getValue(String.class));
                        }

                        String lat = null;
                        String lng = null;
                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                            lat = dataSnapshot.child("lat").getValue(String.class);
                            lng = dataSnapshot.child("lng").getValue(String.class);
                        }
                        ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng);
                        consumerModel.setAmountOfMilk(milkDemand);

                        mConsumerModelArrayList.add(consumerModel);
                        if (lat != null) {
                            createNewMarker(consumerModel);
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        //it is assumed that this method will only be activated when the location for a consumer marker is updated
                        String id = dataSnapshot.getKey();

                        String lat = null;
                        String lng = null;
                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                            lat = dataSnapshot.child("lat").getValue(String.class);
                            lng = dataSnapshot.child("lng").getValue(String.class);
                        }
                        ConsumerModel currentConsumerModel = null;
                        for (ConsumerModel currentModel : mConsumerModelArrayList) {
                            if (currentModel.getId().equals(id)) {
                                currentConsumerModel = currentModel;
                                currentConsumerModel.setLat(lat);
                                currentConsumerModel.setLng(lng);

                                break;
                            }
                        }
                        if (currentConsumerModel == null) return;

                        if (lat != null && currentConsumerModel.getMarker() == null) {//if the marker is null then the location is added for the first time and a new marker is needed
                            createNewMarker(currentConsumerModel);
                        }
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

    private void loadAllConsumersForTodaysJourney() {
        //first we fetch all the data that does not change in time
        FirebaseDatabase.getInstance().getReference()
                .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())//prodcuer id
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot clientSnap : dataSnapshot.getChildren()) {

                                String id = dataSnapshot.getKey();
                                String name = dataSnapshot.child("name").getValue(String.class);
                                String number = dataSnapshot.child("number").getValue(String.class);
                                String timeStamp = dataSnapshot.child("number").getValue(String.class);// FIXME: 9/14/2019 we need time stamp
                                String lat = null;
                                String lng = null;
                                if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                                    lat = dataSnapshot.child("lat").getValue(String.class);
                                    lng = dataSnapshot.child("lng").getValue(String.class);
                                }
                                ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng);

                                mConsumerModelArrayList.add(consumerModel);
                                if (lat != null) {
                                    createNewMarker(consumerModel);
                                }
//                        mActiveRideStopsModelArrayList.get()

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        FirebaseDatabase.getInstance().getReference()
                .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())//prodcuer id
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String id = dataSnapshot.getKey();
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String number = dataSnapshot.child("number").getValue(String.class);
                        String timeStamp = dataSnapshot.child("number").getValue(String.class);// FIXME: 9/14/2019 we need time stamp
                        String lat = null;
                        String lng = null;
                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                            lat = dataSnapshot.child("lat").getValue(String.class);
                            lng = dataSnapshot.child("lng").getValue(String.class);
                        }
                        ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng);

                        mConsumerModelArrayList.add(consumerModel);
                        if (lat != null) {
                            createNewMarker(consumerModel);
                        }


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        //it is assumed that this method will only be activated when the location for a consumer marker is updated
                        String id = dataSnapshot.getKey();

                        String lat = null;
                        String lng = null;
                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                            lat = dataSnapshot.child("lat").getValue(String.class);
                            lng = dataSnapshot.child("lng").getValue(String.class);
                        }
                        ConsumerModel currentConsumerModel = null;
                        for (ConsumerModel currentModel : mConsumerModelArrayList) {
                            if (currentModel.getId().equals(id)) {
                                currentConsumerModel = currentModel;
                                currentConsumerModel.setLat(lat);
                                currentConsumerModel.setLng(lng);

                                break;
                            }
                        }
                        if (currentConsumerModel == null) return;

                        if (lat != null && currentConsumerModel.getMarker() == null) {//if the marker is null then the location is added for the first time and a new marker is needed
                            createNewMarker(currentConsumerModel);
                        }
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
    private void createNewMarker(ConsumerModel consumerModel) {
        String key = consumerModel.getId();
        MarkerOptions markerOptions = new MarkerOptions();
        //added array list size to indicate the number of marker
        markerOptions.title("marker" + mConsumerModelArrayList.size() + ":" + key);
        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.marker_home));
        markerOptions.draggable(true);
        markerOptions.position(new LatLng(Double.parseDouble(consumerModel.getLatitude()),
                Double.parseDouble(consumerModel.getLongitude())));
        Marker marker = mMap.addMarker(markerOptions);
        consumerModel.setMarker(marker);
//        mConsumerModelArrayList.get(mConsumerModelArrayList.size() - 1).setMarker(marker);
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
                for (ConsumerModel consumerModel : mConsumerModelArrayList) {
                    if (consumerModel.getMarker().equals(marker)) {
                        consumerModel.setLat(marker.getPosition().latitude + "");
                        consumerModel.setLng(marker.getPosition().longitude + "");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("lat", marker.getPosition().latitude + "");
                        hashMap.put("lng", marker.getPosition().longitude + "");

                        FirebaseDatabase.getInstance().getReference()
                                .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(consumerModel.getId())
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

    //copied from stack overflow learn later
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
        if (mConsumerModelArrayList.get(selectedStopPos).getLatitude() == null) {
            Toast.makeText(this, "location for this consumer is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIXME: 9/10/2019 later change the logic
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG)).commit();
        getSupportActionBar().setTitle("Map");
        Double lat = Double.parseDouble(mConsumerModelArrayList.get(position).getLatitude());
        Double lng = Double.parseDouble(mConsumerModelArrayList.get(position).getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));

    }

    @Override
    public void onStartRiding() {
        mTotalMilkDemand = calculateMilkDemand();
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG)).commit();
        if (isjournyActive) {
            Snackbar.make(drawer, "you are already riding", Snackbar.LENGTH_LONG).show();
            return;
        }
        //dialog related
        String message = "Total milk Volume : " + mTotalMilkDemand + "\n are you all set? Press Go to proceed";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Riding ?")
                .setMessage(message)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRiding();
                    }
                }).setNegativeButton("cancel", null).show();


    }

    private float calculateMilkDemand() {
        float milkDemand = 0.0f;
        for (ConsumerModel consumerModel : mConsumerModelArrayList) {
            milkDemand += consumerModel.getAmountOfMilk();

        }
        return milkDemand;
    }


    private void startRiding() {
        isjournyActive = true;
        startJourney();
        Snackbar.make(drawer, "processing...", Snackbar.LENGTH_SHORT).show();

        bottomSheetBehavior.setHideable(false);
        bottomSheetLinearLayout.setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetLinearLayout.setVisibility(View.VISIBLE);
        mAciveRideStopsAdapter.notifyDataSetChanged();
        new Handler(getMainLooper()).postDelayed(new Runnable() { //just to show user that the list is active
            @Override
            public void run() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }, 1000);

//        journyInfoContainer.setVisibility(View.VISIBLE);
    }

    private void startJourney() {

        //  this link will help you tomake a polyline as soon as the start journy is callled https://www.youtube.com/watch?v=wRDLjUK8nyU
        if (mCurrentLocationResult == null) {
            Toast.makeText(this, "cant get your location ", Toast.LENGTH_SHORT).show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            return;
        }
        if (mConsumerModelArrayList.isEmpty()) {
            Toast.makeText(this, "there are no stops to reach to.. ", Toast.LENGTH_SHORT).show();
            Snackbar.make(drawer, "there are no stops to reach to.. ", Snackbar.LENGTH_LONG);
            isjournyActive = false;
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

//            journyInfoContainer.setVisibility(View.GONE);
            return;
        }

        final LatLng currentLocation = new LatLng(mCurrentLocationResult.getLocations().get(0).getLatitude(), mCurrentLocationResult.getLocations().get(0).getLongitude());
        if (mConsumerModelArrayList.get(activeStopPosition).getLatitude() == null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            Toast.makeText(this, "This stop location is not set", Toast.LENGTH_SHORT).show();
            return;
        }


        if (shouldDrawfullRoute) {//to make this path drawing be called only once in a ride
            //to generate a polyline over the complete route
            drawFullRoutePolyline(currentLocation);
        }


        final LatLng stop1Location = new LatLng(Double.parseDouble(mConsumerModelArrayList.get(activeStopPosition).getLatitude()), Double.parseDouble(mConsumerModelArrayList.get(activeStopPosition).getLongitude()));
        //to generate a polyline for the specific stop
        new FetchURL(ProducerNavMapActivity.this, false).execute(getDirectionApiUrl(currentLocation, stop1Location, false), "driving");

        String url = UrlGenrator.generateDistanceMatrixUrl(currentLocation, stop1Location, getResources().getString(R.string.distance_matrix_api_key));
        DistanceMatrixAsyncTask distanceMatrixAsyncTask = new DistanceMatrixAsyncTask() {
            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    Toast.makeText(ProducerNavMapActivity.this, "response was null", Toast.LENGTH_LONG).show();
                } else {

                    System.out.println("response :" + s);
                    Snackbar.make(drawer, "riding now", Snackbar.LENGTH_LONG).show();

                    // FIXME: 9/16/2019 crashes here as its still running while app is shi down
                    ProducerFirebaseHelper.updateStatus(getResources().getString(R.string.status_producer_onduty));
                    System.out.println("json string :" + s);
                    DistanceMatrixModel distanceMatrixModel = HttpRequestHelper.parseDistanceMatrixJson(s);

                    if (ProducerNavMapActivity.this != null) {


                        distanceTextView.setText(distanceMatrixModel.getDistance()/*distanceArray[0] + " meters"*/);
                        timeTextView.setText(distanceMatrixModel.getDuration());
                        speedTextView.setText(String.format("%.2f m/sec", mCurrentLocationResult.getLocations().get(0).getSpeed()));
                    }
                    if (distanceMatrixModel.getDurationLong() <= 45) {
                        //todo for noe grtting the first consumer in list later it wont work
                        showNotificationForConsumer(mConsumerModelArrayList.get(activeStopPosition).getId(), distanceMatrixModel.getDurationLong());
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
                                if (mFullRoutePolyline != null) {
                                    mFullRoutePolyline.remove();
                                    shouldDrawfullRoute = true;//to rstore the full path drawing for future rides
                                }

                                ProducerFirebaseHelper.updateStatus(getResources().getString(R.string.status_producer_inactive));
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

    private void drawFullRoutePolyline(LatLng currentLocation) {
        LatLng stopLocation = new LatLng(Double.parseDouble(mConsumerModelArrayList.get(mConsumerModelArrayList.size() - 1).getLatitude()),
                Double.parseDouble(mConsumerModelArrayList.get(mConsumerModelArrayList.size() - 1).getLongitude()));
        new FetchURL(ProducerNavMapActivity.this, true).execute(getDirectionApiUrl(currentLocation, stopLocation, true), "driving");
    }

    // TODO: 8/15/2019 create this one on your own and place in httphelper util class and add direction mode later if needed
    private String getDirectionApiUrl(LatLng origin, LatLng dest, boolean isFullRoute /*, String directionMode*/) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        //String mode = "mode=" + directionMode;


        // Building the parameters to the web service
        String parameters = null;
        if (isFullRoute) {
            StringBuilder wayPoints = new StringBuilder("waypoints=");
            for (int i = 0; i < mConsumerModelArrayList.size(); i++) {

                ConsumerModel curentConsumerModel = mConsumerModelArrayList.get(i);
                Double currentLat = Double.parseDouble(curentConsumerModel.getLatitude());
                Double currentLng = Double.parseDouble(curentConsumerModel.getLongitude());
                wayPoints.append("via:").append(currentLat).append(",").append(currentLng);
                if (i != mConsumerModelArrayList.size() - 1) {//if its not the last one then add pipe
                    wayPoints.append("|");
                }
            }

            parameters = str_origin + "&" + str_dest + "&" + wayPoints.toString();
        } else {
            parameters = str_origin + "&" + str_dest;
        }


        // Output format
        String output = "json";


//        &waypoints = via:-37.81223 % 2 C144 .96254 % 7 Cvia:
//        -34.92788 % 2 C138 .60008
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getResources().getString(R.string.directions_api);
    }

    private void showNotificationForConsumer(String id, float timeRemaining) {
        // TODO: 8/4/2019  for now generating both  in current app and in customers app later only customer will be notified

        String title = "Milk Alert";
        String message = "your milk is about to arrive in " + timeRemaining + "seconds";

        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", title);
        notificationMap.put("message", message);
        FirebaseDatabase.getInstance().getReference().child("notifications")
                .child(id)//reciever id
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//sender id
                .push()//notification id
                .setValue(notificationMap);//for now no need for completion listener


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
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)//fixme  if needed
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(11, downloadNotificationBuilder.build());

    }

    @Override
    public void onTaskDone(Object... values) {
        if ((boolean) values[1]) { //this means its a full route
            if (mFullRoutePolyline != null) {
                mFullRoutePolyline.remove();
            }
            mFullRoutePolyline = mMap.addPolyline(((PolylineOptions) values[0]).geodesic(true));
            shouldDrawfullRoute = false;
//                    .color(Color.CYAN)
//                    .width(10))

        } else {
            if (mCurrentPolyline != null)
                mCurrentPolyline.remove();
            mCurrentPolyline = mMap.addPolyline(((PolylineOptions) values[0]).geodesic(true)
                    .color(Color.RED)
                    .width(12));
            /*.pattern(PattrrPATTERN_POLYGON_ALPHA))*/
            ;

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

    //code borrowed from google sample codes
    //https://gist.github.com/broady/6314689
    private void animateMarkerToICS(Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator/*, final boolean isSelected*/) {
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
//                if (isSelected) { // if this child is in observation
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(finalPosition));
//                }
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
    public void onEditLocation(int position) {

        ConsumerModel consumerModel = mConsumerModelArrayList.get(position);


        if (consumerModel.getLatitude() != null && consumerModel.getLongitude() != null) {//then we are updating an old position of the stop
            // TODO: 9/14/2019 later  we may change the markers background to indicate that this marker is being changed and set the drag listner temporarily and only for the currunt marker and then remove the drag listener from map

            Toast.makeText(this, "Grab the marker and shift to the desired location ", Toast.LENGTH_SHORT).show();

        } else {
            addCurrentLoadtionInFirebaseAsAStop(consumerModel);
        }

        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG)).commit();
        getSupportActionBar().setTitle("Map");


    }

    /**
     * this method is responsible for adding the active location to firebase and will be treated as a stop
     * this location will be used as a marker
     *
     * @param consumerModel the model to which a stop is being added
     */
    private void addCurrentLoadtionInFirebaseAsAStop(ConsumerModel consumerModel) {
        if (mCurrentLocationResult != null) {

            HashMap<String, Object> locationMap = new HashMap<>();
            locationMap.put("lat", String.valueOf(mCurrentLocationResult.getLocations().get(0).getLatitude()));
            locationMap.put("lng", String.valueOf(mCurrentLocationResult.getLocations().get(0).getLongitude()));


            FirebaseDatabase.getInstance().getReference()
                    .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())//producer_id
                    .child(consumerModel.getId())//consumer id
                    .updateChildren(locationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // TODO: 9/14/2019 later remove this success toast
                        Toast.makeText(ProducerNavMapActivity.this, "new Location is set to the database for this client ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProducerNavMapActivity.this, "error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    public ArrayList<ConsumerModel> getConsumersArrayList() {
        return this.mConsumerModelArrayList;
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

//// TODO: 7/27/2019 for the purpose of Gps use a broadcast reciever for making changes as desired

//  in my head it just came that this app can be used for daily groceries , including all stuff that is in daly use .