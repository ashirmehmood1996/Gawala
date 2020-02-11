package com.android.example.gawala.Transporter.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.example.gawala.Consumer.Activities.ConsumerMapActivity;
import com.android.example.gawala.Generel.Activities.LoginActivity;
import com.android.example.gawala.Generel.Activities.NotificationsActivity;
import com.android.example.gawala.Generel.Activities.ProfileActivity;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Provider.Activities.ProviderMainActivity;
import com.android.example.gawala.Transporter.Adapters.AciveRideStopsAdaper;
import com.android.example.gawala.Transporter.Fragments.TransporterRideInfoFragment;
import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.Transporter.Fragments.ProducerSettingsFragment;
import com.android.example.gawala.Transporter.Fragments.TransporterClientsFragment;
import com.android.example.gawala.Transporter.Interfaces.LatLngInterpolator;
import com.android.example.gawala.Transporter.Services.RideService;

import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.android.example.gawala.Transporter.Fragments.ProducerSummeryFragment;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Property;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static androidx.annotation.Dimension.SP;
import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;


public class TransporterMainActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        TransporterRideInfoFragment.Callbacks/*, TaskLoadedCallback*/, RideService.Callbacks
        /*,ProviderClientsFragment.CallBacks */ {


    private RideService mRideService;
    private boolean mIsBound = false;
    private boolean isProviderAlso = false;

    private static final String TAG_FRAG_RIDE_INFO = "FragRideInfo";
    private static final String CLIENTS_FRAG_TAG = "FragClients";
    private String providerId;
    private Uri mPhotoImageUri = null;
    private static final int RC_SET_DELIVERY_LOCATION = 102;
    private final int RC_LOCAION_ON = 101;
    private final String PRODUCER_DASHBOARD_FRAGMENT_TAG = "TransporterRideInfoFragment";
    private final String SUMMERY_FRAGMENT_TAG = "SummeryProducerTag";
    private String PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG = "ProducerClientRequestFragment";
    private final int RC_PERMISSION_ALL = 100;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    private static final long FASTEST_INTERVAL = 5000; //change back to 100000
    private static final long REQUEST_INTERVAL = 5000;//change back to 15000
    private int selectedStopPos = 0;

    private GoogleMap mMap;
    private DrawerLayout drawer;
    private LocationResult mCurrentLocationResult;
    private ArrayList<ConsumerModel> mConsumerModelArrayList;

    private FragmentManager mFragmentManager;
    private Marker myCurrentLocationMarker;
    public static boolean isjournyActive;


    private Polyline mCurrentPolyline;
    private LocationManager locationManager;
    private Polyline mFullRoutePolyline;
    private boolean shouldDrawfullRoute = true;
    public static int activeStopPosition = 0;

    private int notificationsCount = 0;
    private RelativeLayout drawerToggleMenuIconRelativeLayout;
    private TextView notificationsCounterTextView;
    private TextView counterActionViewTextView;
    private Button deliveredToCurrentStopButton;
    //    private View journyInfoContainer;
    private TextView distanceTextView, speedTextView, timeTextView;
//    private Button abortJournyButton;

    //bottom sheet related
    private LinearLayout bottomSheetLinearLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView mActiveRideStopsRecyclerView;
    //    private ArrayList<ActiveRideStopsModel> mActiveRideStopsModelArrayList;
    private AciveRideStopsAdaper mActiveRideStopsAdapter;

    private ImageButton toggleImageButton;
    private int mMilkPrice;
    private String myID;
    private static final String TAG = "ProducerMap";
    private ArrayList<ConsumerModel> mActiveRideArrayList;
    private AlertDialog mProgressDialog;

    private TextToSpeech textToSpeech;
    private String DIALOG_Settings = "dialogSettings";
    private DatabaseReference myLocationNodeRef;
    private ValueEventListener mMyLocationNodelListener;

    private CircularImageView profileCircularImageView;
    private TextView nameTextView, numberTextView;
    public static final String IS_PROVIDER_TOO = "isProviderKey";
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_map);
        initFields();
        attachListeners();
        if (!hasPermissions(this, PERMISSIONS)) {
            requestAllPermissions();
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlerDialogForGPS();
        } else {
//            checkIfDeliveryLocationIsProvided();
        }
        fetchProviderIdIfAny();
        UtilsMessaging.initFCM();


//        initUserDataIfFirstTime();


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
                                ActivityCompat.requestPermissions(TransporterMainActivity.this, PERMISSIONS,
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        Toast.makeText(TransporterMainActivity.this, "location is not enabled app will shut down shortly", Toast.LENGTH_SHORT).show();
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
        AlertDialog alert = builder.create();
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


    private void fetchProviderIdIfAny() {
        mProgressDialog.show();
        rootRef.child(getResources().getString(R.string.transporter)).orderByChild(myID).equalTo(myID).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                            if (iterator.hasNext()) {
                                DataSnapshot dataSnapshot1 = iterator.next();
                                providerId = dataSnapshot1.getKey();
                                loadAllConsumers();
                            } else {
                                Toast.makeText(TransporterMainActivity.this, "Ops ! Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(TransporterMainActivity.this, "No Provider was connected to you", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });

    }

    private void initFields() {
        isProviderAlso = getIntent().getBooleanExtra(IS_PROVIDER_TOO, false);


        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        providerId = getIntent().getStringExtra(PROVIDER_ID_ARRAY);

        myLocationNodeRef = rootRef.child("locationUpdates")
                .child(myID);
        mMyLocationNodelListener = new ValueEventListener() {
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

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Toolbar toolbar = findViewById(R.id.tb_producer_nav);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Map");
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
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
        profileCircularImageView = navigationView.getHeaderView(0).findViewById(R.id.civ_prod_nav_map);
        nameTextView = navigationView.getHeaderView(0).findViewById(R.id.tv_prod_nav_name);
        numberTextView = navigationView.getHeaderView(0).findViewById(R.id.tv_prod_nav_number);
        nameTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        numberTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        initBottomSheet();
        initializeDialog();

        //for counter in drawer menu
        counterActionViewTextView = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_producer_map_clients));

        setUpCounterLayout();

        if (isProviderAlso) {
            setDrawer();
        } else {
            navigationView.getMenu().removeItem(R.id.nav_producer_map_switch_to_provider);

        }
    }

    private void setDrawer() {

        navigationView.getMenu().removeItem(R.id.nav_producer_map_share_code);
        navigationView.getMenu().removeItem(R.id.nav_producer_map_personal_info);
        navigationView.getMenu().removeItem(R.id.nav_producer_map_provider);
        navigationView.getMenu().removeItem(R.id.nav_producer_map_notifications);//fixme LATER if needed what to do with notifitcations
    }

    private void setUpCounterLayout() {
        counterActionViewTextView.setText("9+");
//        counterActionViewTextView.setBackground(getResources().getDrawable(R.drawable.round_circle_red));
        counterActionViewTextView.setGravity(Gravity.CENTER);
        counterActionViewTextView.setTextColor(Color.RED);
        counterActionViewTextView.setTextSize(SP, 16);
        counterActionViewTextView.setTypeface(Typeface.DEFAULT_BOLD);

    }

    private void initBottomSheet() {


        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //recyclerview related
        mActiveRideStopsRecyclerView = findViewById(R.id.rv_bottom_sheet_active_riding);
        mActiveRideArrayList = new ArrayList<>();
//        mActiveRideStopsModelArrayList=new ArrayList<>();
        mActiveRideStopsAdapter = new AciveRideStopsAdaper(this, mActiveRideArrayList);
        mActiveRideStopsRecyclerView.setAdapter(mActiveRideStopsAdapter);

//        mActiveRideStopsAdapter


        toggleImageButton = findViewById(R.id.bt_bottom_sheet_toggle);
        distanceTextView = findViewById(R.id.tv_bottom_sheet_prod_distance);
        timeTextView = findViewById(R.id.tv_bottom_sheet_prod_time);
        speedTextView = findViewById(R.id.tv_bottom_sheet_prod_speed);
//        abortJournyButton = findViewById(R.id.bt_prod_abort_journy);
        deliveredToCurrentStopButton = findViewById(R.id.bt_prod_delivered_to_current_stop);
        setBottomSheetCallBacks();
        drawerToggleMenuIconRelativeLayout = findViewById(R.id.rl_producer_nav_menu_icon_container);
        notificationsCounterTextView = findViewById(R.id.tv_producer_nav_menu_icon_counter);


    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mProgressDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void setBottomSheetCallBacks() {

        //bottom sheet state change listener
        //we are changing button text when sheet changed state
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {

                    case BottomSheetBehavior.STATE_HIDDEN:
                        mMap.setPadding(0, 0, 0, 0);
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:

                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mMap.setPadding(0, 0, 0, bottomSheetBehavior.getPeekHeight());
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
        drawerToggleMenuIconRelativeLayout.setOnClickListener(v -> {
            drawer.openDrawer(Gravity.LEFT);
        });

        deliveredToCurrentStopButton.setOnClickListener(v -> {
            mRideService.deliveredToCurrentStop();
            mActiveRideStopsAdapter.notifyDataSetChanged();


//                mConsumerModelArrayList.get(RideService.activeStopPosition).setDelivered(true);

//            sendMessageToConsumer(mConsumerModelArrayList.get(activeStopPosition).getId());

//            if (activeStopPosition >= mActiveRideArrayList.size() - 1) { //this means that all stops are done
//                abortJourny();
//                speak("saving record..");
//                Toast.makeText(TransporterMainActivity.this, "all stops done, Now finishing Ride....", Toast.LENGTH_SHORT).show();
//            } else {
//                activeStopPosition++;
//                mActiveRideStopsAdapter.notifyDataSetChanged();
//                speak("Now heading towards " + mActiveRideArrayList.get(activeStopPosition).getName());
//                Toast.makeText(TransporterMainActivity.this, "successfully marked as delivered, Now going gor next stop", Toast.LENGTH_SHORT).show();
//            }
        });
        toggleImageButton.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


        });
    }

//journey rekated
    /*private void sendMessageToConsumer(String consumerId) {
        String title = "Acknowledgement";
        String message = "milk is delivered to you";
        String type = "Acknowledgement";
        sendNotificationToConsumer(consumerId, title, message, type);
    }

    private void abortJourny() {
        if (!isjournyActive) {
            Snackbar.make(drawer, "no ride was active", Snackbar.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(drawer, "processing...", Snackbar.LENGTH_SHORT).show();
        addSessionToDatabase();
        mActiveRideArrayList.clear();
        isjournyActive = false;
        activeStopPosition = 0;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    private void addSessionToDatabase() {
        DatabaseReference permanentDataRef = rootRef
                .child("data").child(providerId)//providerID
                .child("permanent_data").push();

        HashMap<String, Object> mainMap = new HashMap<>();
        HashMap<String, Object> clientsMap = new HashMap<>();
        for (ConsumerModel consumerModel : mActiveRideArrayList) {
            String id = consumerModel.getId();
//            String time_stamp=
            String name = consumerModel.getName();
            float amounOfMilk = consumerModel.getAmountOfMilk();
            boolean status = consumerModel.isDelivered();


            HashMap<String, Object> clientDatamap = new HashMap<>();
            clientDatamap.put("name", name);//client name

            HashMap<String, Object> goodsMap = new HashMap<>();
            for (AcquiredGoodModel acquiredGoodModel : consumerModel.getDemandArray()) {
//                String demandUnits=acquiredGoodModel.getDemand();
                GoodModel goodModel = acquiredGoodModel.getGoodModel();
                goodsMap.put(goodModel.getId(), acquiredGoodModel);
            }

//            clientDatamap.put("milk_amount", amounOfMilk);
            clientDatamap.put("goods", goodsMap);
            clientDatamap.put("status", status);

            clientsMap.put(id, clientDatamap);
        }
        mainMap.put("clients", clientsMap);
        mainMap.put("time_stamp", Calendar.getInstance().getTimeInMillis());//the time at ehich this session was put to an end
        mainMap.put("milk_price", mMilkPrice); //adding price just because it can change from day to day
        mainMap.put("transporter_id", myID); //transporter id
        permanentDataRef.setValue(mainMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "session successfully uploaded to cloud database", Toast.LENGTH_SHORT).show();
            }
        });
    }
*/
//    private void initUserDataIfFirstTime() {
//        rootRef.child("data")
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child("milk_price").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    ProducerFirebaseHelper.updateRate("100");
//                    mMilkPrice = 120;
//                } else {
//                    mMilkPrice = Integer.parseInt(dataSnapshot.getValue(String.class));
//
//                }
//                ProducerFirebaseHelper.updateStatus(getResources().getString(R.string.status_producer_inactive));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }

//    private void checkIfDeliveryLocationIsProvided() {
//        rootRef.child("users").child(myID).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists() && TransporterMainActivity.this != null) { //location is not set
//                    showDialogToSendToPickupLocationActivty();
//                } else {//location was set
//                    SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", dataSnapshot.child("lat").getValue(String.class));
//                    SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", dataSnapshot.child("lng").getValue(String.class));
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }

//    private void showDialogToSendToPickupLocationActivty() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setIcon(R.drawable.ic_warning_black_24dp);
//        builder.setTitle("Set your Shop/Business Location");
//        builder.setMessage("The location of your Business is not set. " +
//                "Click proceed to specify it on Map");
//        builder.setPositiveButton("proceed", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                sendToPickLocationActivity();
//
//            }
//        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(TransporterMainActivity.this, "you will not be able to proivide Services " +
//                        "to Consumers unless you specify your Business Location", Toast.LENGTH_LONG).show();
//            }
//        });
//        builder.create().show();
//    }

//    private void sendToPickLocationActivity() {
//        Intent intent = new Intent(this, PickLocationMapsActivity.class);
//        intent.putExtra(getResources().getString(R.string.from_which_activty), getResources().getString(R.string.from_consumer_dash_board));
//        startActivityForResult(intent, RC_SET_DELIVERY_LOCATION);
//
//    }

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

        } else if (requestCode == RC_LOCAION_ON) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "location is not enabled app will shut down shortly", Toast.LENGTH_SHORT).show();
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    //Do something after 100ms
                    finish();
                }, 2000);
            } else {
                recreate();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG)).commit();
//            getSupportActionBar().setTitle("Map");
        } else if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG)).commit();
//            getSupportActionBar().setTitle("Map");
        } else if (mFragmentManager.findFragmentByTag(SUMMERY_FRAGMENT_TAG) != null) {
            mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(SUMMERY_FRAGMENT_TAG)).commit();
//            getSupportActionBar().setTitle("Map");
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_producer_map_logout:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    showLogoutAlertDialog();
                } else {
                    Toast.makeText(this, "already logged out", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_producer_map_clients:
                startClientsFragment();
//                showClientsFragment();
                break;
            case R.id.nav_producer_map_provider:
                fetchProviderIdAndShowDetails();

                break;
            case R.id.nav_producer_map_notifications:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;
            case R.id.nav_producer_map_switch_to_provider:
                sendToProviderActivity();
                break;

            case R.id.nav_producer_map_ride_info:
                checkIfConsumerIsOnVacation();
                break;
            case R.id.nav_producer_map_rides_summery:
                showSummeryFragment();
                break;
            case R.id.nav_producer_map_personal_info:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.nav_producer_map_settings:
                showSettingsFragment();
                break;
            case R.id.nav_producer_map_share_code:
                shareTransporterCode();

                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendToProviderActivity() {
        SharedPreferenceUtil.storeValue(this, getResources().getString(R.string.mode_key), getResources().getString(R.string.provider));

        Intent intent = new Intent(this, ProviderMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void fetchProviderIdAndShowDetails() {
        if (providerId != null) {
            Intent intent = new Intent(TransporterMainActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.USER_ID, providerId);
            intent.putExtra(ProfileActivity.REQUEST_USER_TYPE, getResources().getString(R.string.transporter));
            intent.putExtra(ProfileActivity.OTHER_USER, true);
            startActivity(intent);
        } else {
            Toast.makeText(this, "please add your self  to a Provider first", Toast.LENGTH_SHORT).show();
        }


    }

    private void shareTransporterCode() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialogue_circle_code);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final TextView circle_code_textview = dialog.findViewById(R.id.tv_connection_code);

        circle_code_textview.setText(myID);

        Button btn_share_multimedia = dialog.findViewById(R.id.btn_share_multimedia);
        Button buttonCopy = dialog.findViewById(R.id.btn_copy);
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("connectionCode", myID);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TransporterMainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btn_share_multimedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/*");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, circle_code_textview.getText().toString());
                sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                        startActivity(Intent.createChooser(sharingIntent, "Share Using"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    startActivity(Intent.createChooser(sharingIntent, "Share Using"));
                }
            }
        });
        dialog.show();
    }


//    private void showClientsFragment() {
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        ProviderClientsFragment providerClientsFragment =
//                (ProviderClientsFragment) getSupportFragmentManager().findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG);
//        if (providerClientsFragment != null) {
//            fragmentTransaction.remove(providerClientsFragment);
//        }
//        ProviderClientsFragment dialogFragment = ProviderClientsFragment.getInstance();
////        dialogFragment.setCallback(this);
//        dialogFragment.show(fragmentTransaction, PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG);
//    }

    private void showSettingsFragment() {

        FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
        ProducerSettingsFragment producerSettingsFragment = (ProducerSettingsFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_Settings);
        if (producerSettingsFragment != null) {
            fragmentTransaction1.remove(producerSettingsFragment);
        }
        ProducerSettingsFragment dialogFragment1 = ProducerSettingsFragment.getInstance();
//        dialogFragment.setCallback(this);
        dialogFragment1.show(fragmentTransaction1, DIALOG_Settings);

    }

    private void startClientsFragment() {
        if (providerId != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            TransporterClientsFragment transporterClientsFragment = (TransporterClientsFragment) getSupportFragmentManager().findFragmentByTag(CLIENTS_FRAG_TAG);
            if (transporterClientsFragment != null) {
                transaction.remove(transporterClientsFragment);
            }
            transporterClientsFragment = TransporterClientsFragment.newInstance(providerId);
            transporterClientsFragment.show(transaction, CLIENTS_FRAG_TAG);

        } else {

        }

    }

    private void showSummeryFragment() {

        if (providerId == null) {
            Toast.makeText(this, "please connect to a provider first ", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ProducerSummeryFragment producerSummeryFragment = (ProducerSummeryFragment) getSupportFragmentManager().findFragmentByTag(SUMMERY_FRAGMENT_TAG);
        if (producerSummeryFragment != null) {
            fragmentTransaction.remove(producerSummeryFragment);
        }
        ProducerSummeryFragment dialogFragment = ProducerSummeryFragment.newInstance(providerId, false);
//        dialogFragment.setCallback(this);
//        dialogFragment.setProducerSummeryModel(producerSummeryModel);
        dialogFragment.show(fragmentTransaction, SUMMERY_FRAGMENT_TAG);

    }

    private void startRideFragment() {
        if (providerId == null) {
            Toast.makeText(this, "please first connect to a provider", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TransporterRideInfoFragment transporterRideInfoFragment = (TransporterRideInfoFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAG_RIDE_INFO);
        if (transporterRideInfoFragment != null) {
            transaction.remove(transporterRideInfoFragment);
        }
        transporterRideInfoFragment = TransporterRideInfoFragment.newInstance(mActiveRideArrayList, providerId);
        transporterRideInfoFragment.setCallBacks(this);
        transporterRideInfoFragment.show(transaction, TAG_FRAG_RIDE_INFO);
    }

//    private void startRideInfoFragment() {
//
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        TransporterRideInfoFragment producerRideInfoFragment = (TransporterRideInfoFragment) getSupportFragmentManager().findFragmentByTag(PRODUCER_DASHBOARD_FRAGMENT_TAG);
//        if (producerRideInfoFragment != null) {
//            fragmentTransaction.remove(producerRideInfoFragment);
//        }
//        TransporterRideInfoFragment dialogFragment = TransporterRideInfoFragment.newInstance(/*mActiveRideArrayList*/ "asdasdas");
//        dialogFragment.setCallBacks(this);
////        dialogFragment.setCallback(this);
//        dialogFragment.show(fragmentTransaction, PRODUCER_DASHBOARD_FRAGMENT_TAG);
//    }

    private void showLogoutAlertDialog() {
        //logout code
        AlertDialog.Builder builder = new AlertDialog.Builder(TransporterMainActivity.this);
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
                .child("messaging_token").setValue("null").addOnCompleteListener(task ->
                AuthUI.getInstance()
                        .signOut(TransporterMainActivity.this)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                SharedPreferenceUtil.storeValue(getApplicationContext(), "lat", null);
                                SharedPreferenceUtil.storeValue(getApplicationContext(), "lng", null);
                                SharedPreferenceUtil.clearAllPreferences(getApplicationContext());
                                Toast.makeText(TransporterMainActivity.this, "logout successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(TransporterMainActivity.this, LoginActivity.class));
                                finish();
                            }
                        })).addOnFailureListener(e -> Toast.makeText(TransporterMainActivity.this, "logout failed please check your internet connection", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TO DO: Consider calling
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
        mMap.setPadding(0, 0, 0, 0); //to do this property may be pixed dependednt find a fix later

//        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//            @Override
//            public void onInfoWindowClick(Marker marker) {
//                String markerTitle = marker.getTitle();
//                if (!markerTitle.equals("you")) {
////                    for (ConsumerModel consumerModel : mConsumerModelArrayList) {//matching the child to markerOptions and calling info window for related child
////                        if (markerTitle.equals(consumerModel.getId())) {
////                            Toast.makeText(mRideService, "Consumer is clicked", Toast.LENGTH_SHORT).show();
////                            break;
////                        }
////                    }
//                    Toast.makeText(mRideService, "Consumer is clicked", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        createLocationRequest();
        listenTomyLocation();
//        loadAllConsumers();
        adDraglisternerToMAp();


    }

    @Override
    protected void onDestroy() {
        myLocationNodeRef.removeEventListener(mMyLocationNodelListener);
        super.onDestroy();
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
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener(task -> {
                    if (TransporterMainActivity.this != null) {
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

        myLocationNodeRef.addValueEventListener(mMyLocationNodelListener);
    }

    private void setMarker(double lat, double lng) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once

        LatLng location = new LatLng(lat, lng);

        if (mMap == null) return;
        if (myCurrentLocationMarker == null) {


            View markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

            CircularImageView circularImageView = markerLayout.findViewById(R.id.civ_custom_marker);
            circularImageView.setBorderColor(Color.BLACK);
            circularImageView.setImageDrawable((getResources().getDrawable(R.drawable.ic_person_black_24dp)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                markerLayout.findViewById(R.id.v_custom_marker).getBackground().setTint(Color.BLACK);
                markerLayout.findViewById(R.id.v_custom_marker).setVisibility(View.INVISIBLE);//because there is already a dot below
            }


            /*Uri uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            if (uri != null) {
                myCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("You")
//                    .snippet(mLo)
                        .icon(BitmapDescriptorFactory.fromBitmap(*//*getMarkerBitmapFromView(markerLayout)*//*createDrawableFromView(TransporterMainActivity.this, markerLayout))));

             *//*Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
//                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Toast.makeText(TransporterMainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Toast.makeText(TransporterMainActivity.this, "Loaded", Toast.LENGTH_LONG).show();
                                circularImageView.setImageDrawable(resource);
                                myCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title("You")
//                    .snippet(mLo)
                                        .icon(BitmapDescriptorFactory.fromBitmap(*//**//*getMarkerBitmapFromView(markerLayout)*//**//*createDrawableFromView(TransporterMainActivity.this, markerLayout))));
                                return false;
                            }
                        }).submit();*//*


//            circularImageView.setImageDrawable(getResources().getDrawable(R.drawable.kid));
            } else {*/
            myCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("You")
//                    .snippet(mLo)
                    .icon(BitmapDescriptorFactory.fromBitmap(/*getMarkerBitmapFromView(markerLayout)*/createDrawableFromView(TransporterMainActivity.this, markerLayout))));

//            }

//            myCurrentLocationMarker = mMap.addMarker(new MarkerOptions().title("You").position(location)/*.draggable(true)*/);
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
    private void createLocationRequest() {
        //this request is to chec the necessary settings

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(REQUEST_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(TransporterMainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                requestLocationUpdates();

            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Toast.makeText(TransporterMainActivity.this, "resolvable failure", Toast.LENGTH_SHORT).show();
                    // FIXME: 9/20/2019 later use this reolution code experimentally to resolve the issue
//               try {
//                   // Show the dialog by calling startResolutionForResult(),
//                   // and check the result in onActivityResult().
////                   ResolvableApiException resolvable = (ResolvableApiException) e;
////                   resolvable.startResolutionForResult(ProviderClientsFragment.this,
////                           REQUEST_CHECK_SETTINGS);
//               } catch (IntentSender.SendIntentException sendEx) {
//                   // Ignore the error.
//               }

                } else {
                    Toast.makeText(TransporterMainActivity.this, "non resolvable failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void requestLocationUpdates() {
        //this will cause the location to be uptoTime in a node in firebase but I dont want too much of that
        LocationRequest request = new LocationRequest();
        request.setInterval(REQUEST_INTERVAL);
        request.setFastestInterval(FASTEST_INTERVAL);
        request.setSmallestDisplacement(10);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        //final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    mCurrentLocationResult = locationResult;
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
                    rootRef.child("locationUpdates")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())// FIXME: 9/8/2019 LATER if time  fix float to string cast exception
                            /*.push()*/.setValue(locationResult).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //                                Toast.makeText(TransporterMainActivity.this, "node updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "error updating firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, null);// FIXME: 8/8/2019 LATER if time and needed detach the listener when the user logs out or activity is teminated
        } else {
            Toast.makeText(getApplicationContext(), "no permissions", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadAllConsumers() {
        for (ConsumerModel consumerModel : mConsumerModelArrayList) { // doing this to avoid  the removal of polyline along with the markers

            consumerModel.getMarker().remove();
//            consumerModel.setMarker(null);
        }
        mConsumerModelArrayList.clear();
//        mMap.clear();
        rootRef.child("clients").child(providerId)//prodcuer id
                .orderByChild("transporter_id").equalTo(myID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && TransporterMainActivity.this != null) {

                            new AsyncTask<Void, Void, Boolean>() {
                                @Override
                                protected Boolean doInBackground(Void... voids) {

                                    CountDownLatch countDownLatch = new CountDownLatch((int) dataSnapshot.getChildrenCount());
                                    for (DataSnapshot clientSnap : dataSnapshot.getChildren()) {

                                        String timeStamp = clientSnap.child("time_stamp").getValue(String.class);

                                        rootRef.child("users").child(clientSnap.getKey())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {

                                                        String id = userSnapshot.getKey();
                                                        String name = userSnapshot.child("name").getValue(String.class);
                                                        String number = userSnapshot.child("number").getValue(String.class);
                                                        String imageUri = "";
                                                        if (userSnapshot.hasChild("profile_image_uri")) {
                                                            imageUri = userSnapshot.child("profile_image_uri").getValue(String.class);
                                                        }

                                                        String lat = userSnapshot.child("location").child("lat").getValue(String.class);
                                                        String lng = userSnapshot.child("location").child("lng").getValue(String.class);
                                                        long alertNotificationTime = 2 * 60;// 2 minutes
                                                        if (userSnapshot.hasChild("notification_prefs/alert_notification_time")) {
                                                            alertNotificationTime = userSnapshot.child("notification_prefs/alert_notification_time").getValue(Long.class) * 60;//converting miutes to seconds
                                                        }

                                                        final ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng, imageUri, alertNotificationTime);
                                                        if (lat != null) {
                                                            String address = lat + "\n" + lng;
                                                            if (userSnapshot.child("location").hasChild("address")) {
                                                                address = userSnapshot.child("location").child("address").getValue(String.class);
                                                            }
                                                            consumerModel.setLocationName(address);

                                                        }
                                                        mConsumerModelArrayList.add(consumerModel);
                                                        if (lat != null) {
                                                            createNewMarker(consumerModel);
//                                                            if (!imageUri.isEmpty()) {
//                                                                downloadImageForChild(consumerModel);
//                                                            }

                                                        }

                                                        countDownLatch.countDown();
                                                        System.out.println("count: " + countDownLatch.getCount());
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });


                                    }
                                    try {
                                        countDownLatch.await();
                                        return true;

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    mProgressDialog.dismiss();
                                    if (aBoolean) {
                                        Toast.makeText(getApplicationContext(), "all data is fetched", Toast.LENGTH_SHORT).show();

                                    } else {

                                    }

//                                    if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) != null) {
//                                        ProviderClientsFragment providerClientsFragment = (ProviderClientsFragment) mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG);
//                                        if (providerClientsFragment.consumersAdapter != null) {
//                                            providerClientsFragment.consumersAdapter.notifyDataSetChanged();
//                                        }
//                                    }
                                }
                            }.execute();


                        } else {
//                            if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) != null) {
//                                ProviderClientsFragment providerClientsFragment = (ProviderClientsFragment) mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG);
//                                if (providerClientsFragment.consumersAdapter != null) {
//                                    providerClientsFragment.consumersAdapter.notifyDataSetChanged();
//                                }
//                            }
                            mProgressDialog.dismiss();
                            Toast.makeText(TransporterMainActivity.this, "no clients were added", Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


//        rootRef.child("clients").child(myID)//prodcuer id
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                        //it is assumed that this method will only be activated when the location for a consumer marker is updated
//                        String id = dataSnapshot.getKey();
//
//                        String lat = null;
//                        String lng = null;
//                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
//                            lat = dataSnapshot.child("lat").getValue(String.class);
//                            lng = dataSnapshot.child("lng").getValue(String.class);
//                        }
//                        ConsumerModel currentConsumerModel = null;
//                        for (final ConsumerModel currentModel : mConsumerModelArrayList) {
//                            if (currentModel.getId().equals(id)) {
//                                currentConsumerModel = currentModel;
//                                currentConsumerModel.setLat(lat);
//                                currentConsumerModel.setLng(lng);
//
//
//                                if (lat != null) {
//                                    new GeoCoderAsyncTask(TransporterMainActivity.this) {
//                                        @Override
//                                        protected void onPostExecute(Address address) {
//                                            currentModel.setLocationName(address.getAddressLine(0));
//                                            //call notify dataset cahnged if required
//                                        }
//                                    }.execute(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
//                                }
//                                //update the location string later
//
//                                break;
//                            }
//                        }
//                        if (currentConsumerModel == null) return;
//
//                        if (lat != null && currentConsumerModel.getMarker() == null) {//if the marker is null then the location is added for the first time and a new marker is needed
//                            createNewMarker(currentConsumerModel);
//                        }
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

    }

    private void downloadImageForChild(final ConsumerModel consumerModel) {
        //defining the location of downloads
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ITSfurt Kids Tracker");
        if (!dir.exists()) {//if directory is not available
            dir.mkdir();    //then create a new directory
        }

        //File file = new File(dir, consumerModel.getUserID()+".jpg");
        //final File localFile = File.createTempFile("" + consumerModel.getUserID(), ".jpg", dir);
        final File localFile = new File(dir, "" + consumerModel.getId() + ".jpg");
        StorageReference profilepicRef = FirebaseStorage.getInstance().getReferenceFromUrl(consumerModel.getImageUrl());

//        if (!localFile.exists()) {//// FIXME: 1/11/2020 not considering a new image from firebase
        profilepicRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        int a = 10;
                        Uri imageUri = Uri.fromFile(localFile);
                        consumerModel.setImageUri(imageUri);
                        createNewMarker(consumerModel);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
                Toast.makeText(TransporterMainActivity.this, "cant download image  " + exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });
//        } else {
//            Uri imageUri = Uri.fromFile(localFile);
//            consumerModel.setImageUri(imageUri);
//            createNewMarker(consumerModel);
//        }

    }

    //this method is only valid if it is called for the last posiioned object of array list
    private void createNewMarker(ConsumerModel consumerModel) {
        /*Bitmap flag = BitmapFactory.decodeResource(getResources(), R.drawable.iconfinder_map_marker_flag_left_azure_73041_64px);

//        flag = ConsumerMapActivity.loadBitmapFromView(scaleDown(flag, 180.0f, true));

//        consumerModel.getId();

        MarkerOptions markerOptions = new MarkerOptions();
        //added array list size to indicate the number of marker
        markerOptions.title(consumerModel.getName());
//        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.marker_home));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(flag));


        markerOptions.draggable(true);
        markerOptions.position(new LatLng(Double.parseDouble(consumerModel.getLatitude()),
                Double.parseDouble(consumerModel.getLongitude())));
        Marker marker = mMap.addMarker(markerOptions);
        consumerModel.setMarker(marker);*/


//        if (consumerModel.getImageUri() != null) {
//            Bitmap bitmap = BitmapFactory.decodeFile(consumerModel.getImageUri().getPath());
//            CircularImageView circularImageView = markerLayout.findViewById(R.id.civ_custom_marker);
////            circularImageView.setImageBitmap(bitmap);
//            Glide.with(this).load(bitmap).into(circularImageView);
//        }

//        TextView numTxt = markerLayout.findViewById(R.id.num_txt);
//        numTxt.setText("27");

//        if (consumerModel.getMarker() != null) {
//            consumerModel.getMarker().remove();
//        }


        /*if (consumerModel.getImageUrl() != null) {
            Glide.with(this).load(consumerModel.getImageUrl())
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(TransporterMainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            View markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
                            CircularImageView circularImageView = markerLayout.findViewById(R.id.civ_custom_marker);
                            circularImageView.setBorderColor(getResources().getColor(R.color.colorAccent));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                markerLayout.findViewById(R.id.v_custom_marker).getBackground().setTint(getResources().getColor(R.color.colorAccent));
                            }
                            circularImageView.setImageDrawable(resource);
                            Marker customMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(consumerModel.getLatitude()),
                                            Double.parseDouble(consumerModel.getLongitude())))
                                    .title(consumerModel.getName())
                                    .snippet(consumerModel.getLocationName())
                                    .icon(BitmapDescriptorFactory.fromBitmap(*//*getMarkerBitmapFromView(markerLayout)*//*createDrawableFromView(TransporterMainActivity.this, markerLayout))));
                            consumerModel.setMarker(customMarker);
                            return true;
                        }
                    }).submit();
//            circularImageView.setImageDrawable(getResources().getDrawable(R.drawable.kid));
        } else {
            View markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
            CircularImageView circularImageView = markerLayout.findViewById(R.id.civ_custom_marker);
            circularImageView.setBorderColor(getResources().getColor(R.color.colorAccent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                markerLayout.findViewById(R.id.v_custom_marker).getBackground().setTint(getResources().getColor(R.color.colorAccent));
            }
            Marker customMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(consumerModel.getLatitude()),
                            Double.parseDouble(consumerModel.getLongitude())))
                    .title(consumerModel.getName())
                    .snippet(consumerModel.getLocationName())
                    .icon(BitmapDescriptorFactory.fromBitmap(*//*getMarkerBitmapFromView(markerLayout)*//*createDrawableFromView(this, markerLayout))));
            consumerModel.setMarker(customMarker);
        }

*/
        // FIXME: 1/12/2020 because above code is misbehaving I am just relying to the simple static layouts
        View markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircularImageView circularImageView = markerLayout.findViewById(R.id.civ_custom_marker);
        circularImageView.setBorderColor(getResources().getColor(R.color.colorAccent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            markerLayout.findViewById(R.id.v_custom_marker).getBackground().setTint(getResources().getColor(R.color.colorAccent));
        }
        Marker customMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(consumerModel.getLatitude()),
                        Double.parseDouble(consumerModel.getLongitude())))
                .title(consumerModel.getName())
                .snippet(consumerModel.getLocationName())
                .icon(BitmapDescriptorFactory.fromBitmap(/*getMarkerBitmapFromView(markerLayout)*/createDrawableFromView(this, markerLayout))));
        consumerModel.setMarker(customMarker);

//        mConsumerModelArrayList.get(mConsumerModelArrayList.size() - 1).setMarker(marker);
    }


    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private Bitmap getMarkerBitmapFromView(View customMarkerView) {
//why not happening if does not happen move forward may be Is should because there is not much time
//        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
//        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
//        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    public Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio;
        ratio = Math.min(
                (float) maxImageSize / 60,
                maxImageSize / 60);
        int width = Math.round(ratio * 60);
        int height = Math.round(ratio * 60);

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
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

                        rootRef
                                .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(consumerModel.getId())
                                .updateChildren(hashMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "marker is placed ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

//    //copied from stack overflow learn later
//    //https://stackoverflow.com/a/45564994/6039129
//    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
//        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
//        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
//        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        vectorDrawable.draw(canvas);
//        return BitmapDescriptorFactory.fromBitmap(bitmap);
//    }


    @Override
    public void onStopMarkerItemClick(int position) {
        selectedStopPos = position;
//        if (mConsumerModelArrayList.get(selectedStopPos).getLatitude() == null) {
        if (mActiveRideArrayList.get(selectedStopPos).getLatitude() == null) {
            Toast.makeText(this, "location for this consumer is not set", Toast.LENGTH_SHORT).show();
            return;
        }
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(TAG_FRAG_RIDE_INFO)).commit();
//        getSupportActionBar().setTitle("Map");
        double lat = Double.parseDouble(mActiveRideArrayList.get(position).getLatitude());
        double lng = Double.parseDouble(mActiveRideArrayList.get(position).getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16));

        mConsumerModelArrayList.get(position).getMarker().showInfoWindow();
    }

    @Override
    public void onStartRiding(HashMap<String, Object> goodsToCarryHashMap) {
        String mTotalMilkDemand = "xyz";
        if (isjournyActive) {
            Snackbar.make(drawer, "you are already riding", Snackbar.LENGTH_LONG).show();
            return;
        }
        //dialog related

        LinearLayout goodsToCarryContainerLinearLayout = new LinearLayout(this);
        goodsToCarryContainerLinearLayout.setOrientation(LinearLayout.VERTICAL);
        goodsToCarryContainerLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        goodsToCarryContainerLinearLayout.setPadding(16, 16, 16, 16);
        for (String key : goodsToCarryHashMap.keySet()) {
            HashMap<String, Object> subMap = (HashMap<String, Object>) goodsToCarryHashMap.get(key);
            String name = (String) Objects.requireNonNull(subMap).get("name");
            String image = (String) subMap.get("image");
            Integer demand = (Integer) subMap.get("demand");
            String unit = (String) subMap.get("unit");
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.li_good_to_carry, null);

            ImageView imageView = linearLayout.findViewById(R.id.iv_li_good_to_carry_image);
            TextView nameTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_name);
            TextView demandTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_units);
            Glide.with(this).load(image).into(imageView);
            nameTextView.setText(name);
            demandTextView.setText(String.format("%d %s", demand, unit));
            goodsToCarryContainerLinearLayout.addView(linearLayout);
        }
//        String message = "Total milk Volume : " + mTotalMilkDemand + "\n are you all set? Press Go to proceed";
        String message = "Please make sure to check the following Item(s) with your self to avoid any in inconvenience.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Riding ?")
                .setMessage(message)
                .setView(goodsToCarryContainerLinearLayout)
                .setPositiveButton("Go", (dialog, which) -> startRiding()).setNegativeButton("cancel", null).show();


    }


    private void startRiding() {
        if (providerId == null) {
            Toast.makeText(this, "please connect yourself to a provider", Toast.LENGTH_LONG).show();
            return;
        }

        isjournyActive = true;
//        startJourney();
//        Snackbar.make(drawer, "processing...", Snackbar.LENGTH_SHORT).show();
//        bottomSheetBehavior.setHideable(false);
//        bottomSheetLinearLayout.setVisibility(View.GONE);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        bottomSheetLinearLayout.setVisibility(View.VISIBLE);
//        mActiveRideStopsAdapter.notifyDataSetChanged();
//        //just to show user that the list is active
//        new Handler(getMainLooper()).postDelayed(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED), 1000);

        // Bind to LocalService
        Intent intent = new Intent(this, RideService.class);

        //removing the marker because its is not serializable later we find a way to remake the marker
        for (ConsumerModel consumerModel : mActiveRideArrayList) {
            consumerModel.setMarker(null);
        }
        intent.putExtra(RideService.RIDE_ARRAY_LIST_KEY, mConsumerModelArrayList);
        intent.putExtra(RideService.PROVIDER_ID, providerId);
        startService(intent);
        bindService(intent, serviceConnection, 0);

//        journyInfoContainer.setVisibility(View.VISIBLE);
    }

    //journey related

    /*private void startJourney() {
        //  this link will help you tomake a polyline as soon as the start journy is callled https://www.youtube.com/watch?v=wRDLjUK8nyU
        if (mCurrentLocationResult == null) {
            Toast.makeText(this, "cant get your location ", Toast.LENGTH_SHORT).show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        if (mActiveRideArrayList.isEmpty()) {
            Toast.makeText(this, "there are no stops to reach to.. ", Toast.LENGTH_SHORT).show();
            Snackbar.make(drawer, "there are no stops to reach to.. ", Snackbar.LENGTH_LONG);
            isjournyActive = false;
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

//            journyInfoContainer.setVisibility(View.GONE);
            return;
        }

        final LatLng currentLocation = new LatLng(mCurrentLocationResult.getLocations().get(0).getLatitude(), mCurrentLocationResult.getLocations().get(0).getLongitude());
        if (mActiveRideArrayList.get(activeStopPosition).getLatitude() == null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            Toast.makeText(this, "This stop location is not set", Toast.LENGTH_SHORT).show();
            return;
        }


        if (shouldDrawfullRoute) {//to make this path drawing be called only once in a ride
            //to generate a polyline over the complete route
            drawFullRoutePolyline(currentLocation);
            speak("ride started...");
            speak("moving towards " + mActiveRideArrayList.get(0).getName());

        }


        final LatLng stop1Location = new LatLng(Double.parseDouble(mActiveRideArrayList.get(activeStopPosition).getLatitude()),
                Double.parseDouble(mActiveRideArrayList.get(activeStopPosition).getLongitude()));
        //to generate a polyline for the specific stop
        new FetchURL(TransporterMainActivity.this, false).execute(getDirectionApiUrl(currentLocation, stop1Location, false), "driving");

        String url = UrlGenrator.generateDistanceMatrixUrl(currentLocation, stop1Location, getResources().getString(R.string.distance_matrix_api_key));
        DistanceMatrixAsyncTask distanceMatrixAsyncTask = new DistanceMatrixAsyncTask() {
            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    Toast.makeText(TransporterMainActivity.this, "response was null", Toast.LENGTH_LONG).show();
                } else {

                    System.out.println("response :" + s);
                    Snackbar.make(drawer, "riding now", Snackbar.LENGTH_LONG).show();

                    // FI XME: 9/16/2019 crashes here as its still running while app is shi down
                    ProducerFirebaseHelper.updateStatus(getResources().getString(R.string.status_producer_onduty));
                    System.out.println("json string :" + s);
                    DistanceMatrixModel distanceMatrixModel = HttpRequestHelper.parseDistanceMatrixJson(s);

                    if (TransporterMainActivity.this != null) {


                        distanceTextView.setText(distanceMatrixModel.getDistance()*//*distanceArray[0] + " meters"*//*);
                        timeTextView.setText(distanceMatrixModel.getDuration());
                        speedTextView.setText(String.format("%.2f m/sec", mCurrentLocationResult.getLocations().get(0).getSpeed()));
                    }

                    if (distanceMatrixModel.getDurationLong() <= 45) {
                        long timeRemaining = distanceMatrixModel.getDurationLong();
                        String title = "Milk Alert";
                        String message = "your milk is about to arrive in " + timeRemaining + "seconds";
                        sendNotificationToConsumer(mActiveRideArrayList.get(activeStopPosition).getId(), title, message, "alert");
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
                                Toast.makeText(TransporterMainActivity.this, "journey aborted", Toast.LENGTH_SHORT).show();
                                speak("ride finished");
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
        LatLng stopLocation = new LatLng(Double.parseDouble(mActiveRideArrayList.get(mActiveRideArrayList.size() - 1).getLatitude()),
                Double.parseDouble(mActiveRideArrayList.get(mActiveRideArrayList.size() - 1).getLongitude()));
        new FetchURL(TransporterMainActivity.this, true).execute(getDirectionApiUrl(currentLocation, stopLocation, true), "driving");
    }

    // TO DO: 8/15/2019 create this one on your own and place in httphelper util class and add direction mode later if needed
    private String getDirectionApiUrl(LatLng origin, LatLng dest, boolean isFullRoute *//*, String directionMode*//*) {
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
            for (int i = 0; i < mActiveRideArrayList.size(); i++) {

                ConsumerModel curentConsumerModel = mActiveRideArrayList.get(i);
                Double currentLat = Double.parseDouble(curentConsumerModel.getLatitude());
                Double currentLng = Double.parseDouble(curentConsumerModel.getLongitude());
                wayPoints.append("via:").append(currentLat).append(",").append(currentLng);
                if (i != mActiveRideArrayList.size() - 1) {//if its not the last one then add pipe
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

    private void sendNotificationToConsumer(String id, String title, String message, String type) {
        // TO DO: 8/4/2019  for now generating both  in current app and in customers app later only customer will be notified

        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", title);
        notificationMap.put("message", message);
        notificationMap.put("type", type);
        notificationMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");

        rootRef.child("notifications")
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
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)//fi xme  if needed
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
            *//*.pattern(PattrrPATTERN_POLYGON_ALPHA))*//*
            ;

        }
    }
*/
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

    /*@Override
    public void onEditLocation(int position) {

        ConsumerModel consumerModel = mConsumerModelArrayList.get(position);

        if (consumerModel.getLatitude() != null && consumerModel.getLongitude() != null) {//then we are updating an old position of the stop
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(Double.parseDouble(consumerModel.getLatitude()),
                            Double.parseDouble(consumerModel.getLongitude())), 16));
            consumerModel.getMarker().showInfoWindow();

            Toast.makeText(this, "Grab the marker and shift to the desired location ", Toast.LENGTH_SHORT).show();

        } else {
            addCurrentLoadtionInFirebaseAsAStop(consumerModel);
        }

        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG)).commit();
        getSupportActionBar().setTitle("Map");


    }

    */

    /**
     * this method is responsible for adding the active location to firebase and will be treated as a stop
     * this location will be used as a marker
     *
     * @param //consumerModel the model to which a stop is being added
     *//*
    private void addCurrentLoadtionInFirebaseAsAStop(ConsumerModel consumerModel) {
        if (mCurrentLocationResult != null) {

            HashMap<String, Object> locationMap = new HashMap<>();
            locationMap.put("lat", String.valueOf(mCurrentLocationResult.getLocations().get(0).getLatitude()));
            locationMap.put("lng", String.valueOf(mCurrentLocationResult.getLocations().get(0).getLongitude()));
            rootRef
                    .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())//producer_id
                    .child(consumerModel.getId())//consumer id
                    .updateChildren(locationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // TO DO: 9/14/2019 later remove this success toast
                        Toast.makeText(TransporterMainActivity.this, "new Location is set to the database for this client ", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(TransporterMainActivity.this, "error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
*/
//    public ArrayList<ConsumerModel> getConsumersArrayList() {
//        return this.mConsumerModelArrayList;
//    }
    private void checkIfConsumerIsOnVacation() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                CountDownLatch countDownLatch = new CountDownLatch(mConsumerModelArrayList.size());//

//        int i = 0;
                for (ConsumerModel consumerModel : mConsumerModelArrayList) {

//            int finalI = i;
                    rootRef.child("days_off").child(consumerModel.getId())
                            .child("days").child(calendar.getTimeInMillis() + "").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d(TAG, "doInBackground: Someone is on vacation");
                                consumerModel.setOnVacation(true);
                            }

//                    if (finalI == mConsumerModelArrayList.size() - 1) {//then it is final result
//                        fetchFreshDataOfUsers();
//                    }
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
//            i++;
                }


                try {
                    countDownLatch.await();
                    Log.d(TAG, "doInBackground: wait is called");
                    return true;
                } catch (InterruptedException e) {
                    Log.d(TAG, "doInBackground: exception is thrown");

                    e.printStackTrace();
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success && TransporterMainActivity.this != null) {

                    fetchFreshy();
//                    fetchFreshDataOfUsers();
                } else {
                    //await was not done and exception was thrown
                }

            }
        }.execute();

    }


    private void fetchFreshy() {
        mProgressDialog.show();
        mActiveRideArrayList.clear();
        fetchFreshyfor(0);

    }

    /**
     * @param position postion of one of the child from connected consumers
     */
    private void fetchFreshyfor(int position) {
        if (mConsumerModelArrayList.isEmpty()) { //if there are no consumers
            Toast.makeText(this, "you donot have any consumers connected", Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
            return;
        }

        if (position >= mConsumerModelArrayList.size()) { // if this is the beyond last consumer
//            mConsumerMarkersAdapter.notifyDataSetChanged();
            startRideFragment();
            mProgressDialog.dismiss();
            return;
        }
//        if (mConsumerModelArrayList.size() == position + 1) { // if this is the last consumer
//            startRideInfoFragment();
//            return;
//        }
        ConsumerModel consumerModel = mConsumerModelArrayList.get(position);

        if (consumerModel.isOnVacation()) {
            fetchFreshyfor(position + 1);
        } else {
            //fetch demand data for this consumer


            rootRef.child("demand")
                    .child(providerId)//producer id
                    .child(consumerModel.getId())//consumer id
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists() && this != null) {

                                new AsyncTask<Void, Void, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(Void... voids) {


                                        ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
                                        consumerModel.setDemandArray(demandArray);
                                        mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff

                                        CountDownLatch countDownLatch = new CountDownLatch((int) dataSnapshot.getChildrenCount());

                                        for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {


                                            String goodID = goodSnap.getKey();
                                            String demandUnits = goodSnap.child("demand").getValue(String.class);
                                            if (demandUnits.equals("0")) {
                                                countDownLatch.countDown();
                                                continue;
                                            } else {
                                                consumerModel.setHasDemand(true);
                                                rootRef.child("goods").child(providerId).child(goodID)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists() && this != null) {
                                                                    if (dataSnapshot.exists()) {
                                                                        GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
                                                                        demandArray.add(new AcquiredGoodModel(demandUnits, providerId, goodModel));
//                                                                       demandArray.notifyDataSetChanged();
                                                                    } else {
//                                                                       Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                                countDownLatch.countDown();
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                countDownLatch.countDown();
                                                                Toast.makeText(TransporterMainActivity.this, "datbase error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }

                                        try {
                                            countDownLatch.await();
                                            Log.d(TAG, "doInBackground: wait is called");
                                            return true;
                                        } catch (InterruptedException e) {
                                            Log.d(TAG, "doInBackground: exception is thrown");

                                            e.printStackTrace();
                                            return false;
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean aBoolean) {


                                        fetchFreshyfor(position + 1);
                                    }
                                }.execute();

//                                ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
//                                consumerModel.setDemandArray(demandArray);
//                                mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
//                                int j = 1;
//
//                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
//
//                                    String good_id = goodSnap.getKey();
//                                    String demandUnits = goodSnap.child("demand").getValue(String.class);
//                                    if (demandUnits.equals("0")) {
//                                        continue;
//                                    } else {
//                                        consumerModel.setHasDemand(true);
//                                    }
//                                    if ((mConsumerModelArrayList.size() == finalI + 1)
//                                            && j == dataSnapshot.getChildrenCount()) {// if its last outer adn inner array list call then the progress dialog shoud disappear
//                                        isFinalCall[0] = true;
//                                    }
//                                    fetchGoodDetailFromFireabse(good_id, demandUnits, demandArray, isFinalCall[0]);
//
//                                    j++;
//                                }
//                                if (!consumerModel.hasDemand()) {
//                                    mActiveRideArrayList.remove(consumerModel);
//                                    if ((mConsumerModelArrayList.size() == finalI + 1)) {
//                                        startRideInfoFragment();
//                                        mProgressDialog.dismiss();
//                                        Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
////                                acquiredGoodsAdapter.notifyDataSetChanged();
//                            } else {
//                                if ((mConsumerModelArrayList.size() == finalI + 1)) {
//                                    startRideInfoFragment();
//                                    mProgressDialog.dismiss();
//                                    Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
//                                }
//
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
                            } else {
                                fetchFreshyfor(position + 1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

//
//    private void fetchFreshDataOfUsers() {
//
//        final boolean[] isFinalCall = {false};//to make sure that all call has been made while assuming the task is done sequeciallt whihc it is not
//        mActiveRideArrayList.clear();
//
//
//        for (int i = 0; i < mConsumerModelArrayList.size(); i++) {
//            final ConsumerModel consumerModel = mConsumerModelArrayList.get(i);//this loop will get demand for each consumer
//            if (consumerModel.isOnVacation()) {//if this consumer is on vacation then no need to fetch dat for this consumer
//                if (i == mConsumerModelArrayList.size() - 1) {
//                    startRideInfoFragment();
//                    mProgressDialog.dismiss();
//                }
//                continue;
//            }
//            final int finalI = i;
//            rootRef.child("demand")
//                    .child(myID)//producer id
//                    .child(consumerModel.getId())//consumer id
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
//                                ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
//                                consumerModel.setDemandArray(demandArray);
//                                mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
//                                int j = 1;
//
//                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
//                                    String good_id = goodSnap.getKey();
//                                    String demandUnits = goodSnap.child("demand").getValue(String.class);
//                                    if (demandUnits.equals("0")) {
//                                        continue;
//                                    } else {
//                                        consumerModel.setHasDemand(true);
//                                    }
//                                    if ((mConsumerModelArrayList.size() == finalI + 1)
//                                            && j == dataSnapshot.getChildrenCount()) {// if its last outer adn inner array list call then the progress dialog shoud disappear
//                                        isFinalCall[0] = true;
//                                    }
//                                    fetchGoodDetailFromFireabse(good_id, demandUnits, demandArray, isFinalCall[0]);
//
//                                    j++;
//                                }
//                                if (!consumerModel.hasDemand()) {
//                                    mActiveRideArrayList.remove(consumerModel);
//                                    if ((mConsumerModelArrayList.size() == finalI + 1)) {
//                                        startRideInfoFragment();
//                                        mProgressDialog.dismiss();
//                                        Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
////                                acquiredGoodsAdapter.notifyDataSetChanged();
//                            } else {
//                                if ((mConsumerModelArrayList.size() == finalI + 1)) {
//                                    startRideInfoFragment();
//                                    mProgressDialog.dismiss();
//                                    Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
//                                }
//
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
////                            Toast.makeText(AcquiredGoodsActivity.this, String.format("fetching Services step for %s was cancelled due to error:%s", producerID, databaseError.getMessage()), Toast.LENGTH_SHORT).show();
//                            mProgressDialog.dismiss();
//
//                        }
//                    });
//        }
//    }


//    private void fetchGoodDetailFromFireabse(String goodID, final String demand, final ArrayList<AcquiredGoodModel> demandArray, final boolean isFinalCall) {
//
//        // TO DO: 10/14/2019 make some indicator that the data is being fetched
//        rootRef
//                .child("goods").child(myID).child(goodID)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
//                            if (dataSnapshot.exists()) {
//                                GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
//                                demandArray.add(new AcquiredGoodModel(demand, myID, goodModel));
//
//
////                            demandArray.notifyDataSetChanged();
//                            } else {
////                            Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
//                            }
//
//                            if (isFinalCall) {
//                                startRideInfoFragment();
//                                mProgressDialog.dismiss();
//                                Toast.makeText(getApplicationContext(), "All data is fetched", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Toast.makeText(getApplicationContext(), "datbase error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }


//    private void checkIfConsumerIsOnVacation() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MILLISECOND, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... voids) {
//                CountDownLatch countDownLatch = new CountDownLatch(mConsumerModelArrayList.size());//
//
////        int i = 0;
//                for (ConsumerModel consumerModel : mConsumerModelArrayList) {
//
////            int finalI = i;
//                    rootRef.child("days_off").child(consumerModel.getId())
//                            .child("days").child(calendar.getTimeInMillis() + "").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists()) {
//                                Log.d(TAG, "doInBackground: Someone is on vacation");
//                                consumerModel.setOnVacation(true);
//                            }
//
////                    if (finalI == mConsumerModelArrayList.size() - 1) {//then it is final result
////                        fetchFreshDataOfUsers();
////                    }
//                            countDownLatch.countDown();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
////            i++;
//                }
//
//
//                try {
//                    countDownLatch.await();
//                    Log.d(TAG, "doInBackground: wait is called");
//                    return true;
//                } catch (InterruptedException e) {
//                    Log.d(TAG, "doInBackground: exception is thrown");
//
//                    e.printStackTrace();
//                    return false;
//                }
//
//            }
//
//            @Override
//            protected void onPostExecute(Boolean success) {
//                if (success) {
//                    fetchFreshy();
////                    fetchFreshDataOfUsers();
//                } else {
//                    //await was not done and exception was thrown
//                }
//
//            }
//        }.execute();
//
//    }
//
//
//    private void fetchFreshy() {
//        mProgressDialog.show();
//        mActiveRideArrayList.clear();
//        fetchFreshyfor(0);
//
//    }
//
//    /**
//     * @param position postion of one of the child from connected consumers
//     */
//    private void fetchFreshyfor(int position) {
//        if (mConsumerModelArrayList.isEmpty()) { //if there are no consumers
//            Toast.makeText(this, "you donot have any consumers connected", Toast.LENGTH_SHORT).show();
//            mProgressDialog.dismiss();
//            return;
//        }
//
//        if (position >= mConsumerModelArrayList.size()) { // if this is the beyond last consumer
//            startRideInfoFragment();
//            mProgressDialog.dismiss();
//            return;
//        }
////        if (mConsumerModelArrayList.size() == position + 1) { // if this is the last consumer
////            startRideInfoFragment();
////            return;
////        }
//        ConsumerModel consumerModel = mConsumerModelArrayList.get(position);
//
//        if (consumerModel.isOnVacation()) {
//            fetchFreshyfor(position + 1);
//        } else {
//            //fetch demand data for this consumer
//
//
//            rootRef.child("demand")
//                    .child(myID)//producer id
//                    .child(consumerModel.getId())//consumer id
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                            if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
//
//                                new AsyncTask<Void, Void, Boolean>() {
//                                    @Override
//                                    protected Boolean doInBackground(Void... voids) {
//
//
//                                        ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
//                                        consumerModel.setDemandArray(demandArray);
//                                        mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
//
//                                        CountDownLatch countDownLatch = new CountDownLatch((int) dataSnapshot.getChildrenCount());
//
//                                        for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
//
//
//                                            String goodID = goodSnap.getKey();
//                                            String demandUnits = goodSnap.child("demand").getValue(String.class);
//                                            if (demandUnits.equals("0")) {
//                                                countDownLatch.countDown();
//                                                continue;
//                                            } else {
//                                                consumerModel.setHasDemand(true);
//                                                rootRef
//                                                        .child("goods").child(myID).child(goodID)
//                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                                                            @Override
//                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                                if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
//                                                                    if (dataSnapshot.exists()) {
//                                                                        GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
//                                                                        demandArray.add(new AcquiredGoodModel(demandUnits, myID, goodModel));
////                                                                       demandArray.notifyDataSetChanged();
//                                                                    } else {
////                                                                       Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
//                                                                    }
//                                                                }
//                                                                countDownLatch.countDown();
//                                                            }
//
//                                                            @Override
//                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                                countDownLatch.countDown();
//                                                                Toast.makeText(getApplicationContext(), "datbase error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                                                            }
//                                                        });
//                                            }
//                                        }
//
//                                        try {
//                                            countDownLatch.await();
//                                            Log.d(TAG, "doInBackground: wait is called");
//                                            return true;
//                                        } catch (InterruptedException e) {
//                                            Log.d(TAG, "doInBackground: exception is thrown");
//
//                                            e.printStackTrace();
//                                            return false;
//                                        }
//                                    }
//
//                                    @Override
//                                    protected void onPostExecute(Boolean aBoolean) {
//
//
//                                        fetchFreshyfor(position + 1);
//                                    }
//                                }.execute();
//
////                                ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
////                                consumerModel.setDemandArray(demandArray);
////                                mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
////                                int j = 1;
////
////                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
////
////                                    String good_id = goodSnap.getKey();
////                                    String demandUnits = goodSnap.child("demand").getValue(String.class);
////                                    if (demandUnits.equals("0")) {
////                                        continue;
////                                    } else {
////                                        consumerModel.setHasDemand(true);
////                                    }
////                                    if ((mConsumerModelArrayList.size() == finalI + 1)
////                                            && j == dataSnapshot.getChildrenCount()) {// if its last outer adn inner array list call then the progress dialog shoud disappear
////                                        isFinalCall[0] = true;
////                                    }
////                                    fetchGoodDetailFromFireabse(good_id, demandUnits, demandArray, isFinalCall[0]);
////
////                                    j++;
////                                }
////                                if (!consumerModel.hasDemand()) {
////                                    mActiveRideArrayList.remove(consumerModel);
////                                    if ((mConsumerModelArrayList.size() == finalI + 1)) {
////                                        startRideInfoFragment();
////                                        mProgressDialog.dismiss();
////                                        Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
////                                    }
////                                }
//////                                acquiredGoodsAdapter.notifyDataSetChanged();
////                            } else {
////                                if ((mConsumerModelArrayList.size() == finalI + 1)) {
////                                    startRideInfoFragment();
////                                    mProgressDialog.dismiss();
////                                    Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
////                                }
////
////                            }
////
////                        }
////
////                        @Override
////                        public void onCancelled(@NonNull DatabaseError databaseError) {
////
//                            } else {
//                                fetchFreshyfor(position + 1);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//        }
//    }

//
//    private void fetchFreshDataOfUsers() {
//
//        final boolean[] isFinalCall = {false};//to make sure that all call has been made while assuming the task is done sequeciallt whihc it is not
//        mActiveRideArrayList.clear();
//
//
//        for (int i = 0; i < mConsumerModelArrayList.size(); i++) {
//            final ConsumerModel consumerModel = mConsumerModelArrayList.get(i);//this loop will get demand for each consumer
//            if (consumerModel.isOnVacation()) {//if this consumer is on vacation then no need to fetch dat for this consumer
//                if (i == mConsumerModelArrayList.size() - 1) {
//                    startRideInfoFragment();
//                    mProgressDialog.dismiss();
//                }
//                continue;
//            }
//            final int finalI = i;
//            rootRef.child("demand")
//                    .child(myID)//producer id
//                    .child(consumerModel.getId())//consumer id
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
//                                ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
//                                consumerModel.setDemandArray(demandArray);
//                                mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
//                                int j = 1;
//
//                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
//                                    String good_id = goodSnap.getKey();
//                                    String demandUnits = goodSnap.child("demand").getValue(String.class);
//                                    if (demandUnits.equals("0")) {
//                                        continue;
//                                    } else {
//                                        consumerModel.setHasDemand(true);
//                                    }
//                                    if ((mConsumerModelArrayList.size() == finalI + 1)
//                                            && j == dataSnapshot.getChildrenCount()) {// if its last outer adn inner array list call then the progress dialog shoud disappear
//                                        isFinalCall[0] = true;
//                                    }
//                                    fetchGoodDetailFromFireabse(good_id, demandUnits, demandArray, isFinalCall[0]);
//
//                                    j++;
//                                }
//                                if (!consumerModel.hasDemand()) {
//                                    mActiveRideArrayList.remove(consumerModel);
//                                    if ((mConsumerModelArrayList.size() == finalI + 1)) {
//                                        startRideInfoFragment();
//                                        mProgressDialog.dismiss();
//                                        Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
////                                acquiredGoodsAdapter.notifyDataSetChanged();
//                            } else {
//                                if ((mConsumerModelArrayList.size() == finalI + 1)) {
//                                    startRideInfoFragment();
//                                    mProgressDialog.dismiss();
//                                    Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
//                                }
//
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
////                            Toast.makeText(AcquiredGoodsActivity.this, String.format("fetching Services step for %s was cancelled due to error:%s", producerID, databaseError.getMessage()), Toast.LENGTH_SHORT).show();
//                            mProgressDialog.dismiss();
//
//                        }
//                    });
//        }
//    }


//    private void fetchGoodDetailFromFireabse(String goodID, final String demand, final ArrayList<AcquiredGoodModel> demandArray, final boolean isFinalCall) {
//
//        // TO DO: 10/14/2019 make some indicator that the data is being fetched
//        rootRef
//                .child("goods").child(myID).child(goodID)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
//                            if (dataSnapshot.exists()) {
//                                GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
//                                demandArray.add(new AcquiredGoodModel(demand, myID, goodModel));
//
//
////                            demandArray.notifyDataSetChanged();
//                            } else {
////                            Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
//                            }
//
//                            if (isFinalCall) {
//                                startRideInfoFragment();
//                                mProgressDialog.dismiss();
//                                Toast.makeText(getApplicationContext(), "All data is fetched", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Toast.makeText(getApplicationContext(), "datbase error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }

    //text to speech related
    @Override
    protected void onStart() {
        initTextToSpeach();
        if (mPhotoImageUri == null) {
            mPhotoImageUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            if (mPhotoImageUri != null) {
                Glide.with(getApplicationContext())
                        .load(mPhotoImageUri).into(profileCircularImageView);
            }
        }

        // Bind to LocalService
        Intent intent = new Intent(this, RideService.class);//on bind service does not provoke onstartCommand() as I read
        bindService(intent, serviceConnection, 0);
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (mIsBound)
            unbindService(serviceConnection);
        super.onStop();
    }


    private void initTextToSpeach() {
        textToSpeech = new TextToSpeech(this, i -> {
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(TransporterMainActivity.this, "text to speech not working", Toast.LENGTH_SHORT).show();
//                        Log.d("mylog", "onInit: error in TEXT TO SPEECH");
                }

            } else {
                Toast.makeText(TransporterMainActivity.this, "error in on init of text to speec", Toast.LENGTH_SHORT).show();
//                    Log.d("mylog", "onInit: error in TEXT TO SPEECH 1");
            }
        });


    }

    private void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIsBound = true;
            RideService.MyLocalBinder myLocalBinder = (RideService.MyLocalBinder) service;
            mRideService = myLocalBinder.getService();
            mRideService.setCallbacks(TransporterMainActivity.this);
            Toast.makeText(TransporterMainActivity.this, "Service is bound", Toast.LENGTH_SHORT).show();
            prepareForActiveRide();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRideService.setCallbacks(null);
            Toast.makeText(TransporterMainActivity.this, "Service is unBound", Toast.LENGTH_SHORT).show();
            mIsBound = false;
        }
    };

    private void prepareForActiveRide() {
        // TO DO: 12/18/2019 there is more to do now
        setPolyLine();

        mActiveRideArrayList = mRideService.getActiveRideArrayList();
        mActiveRideStopsAdapter = new AciveRideStopsAdaper(TransporterMainActivity.this, mActiveRideArrayList);
        mActiveRideStopsRecyclerView.setAdapter(mActiveRideStopsAdapter);

        isjournyActive = true;
//        startJourney();
        Snackbar.make(drawer, "processing...", Snackbar.LENGTH_SHORT).show();
        bottomSheetBehavior.setHideable(false);
        bottomSheetLinearLayout.setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetLinearLayout.setVisibility(View.VISIBLE);
        mActiveRideStopsAdapter.notifyDataSetChanged();
        //just to show user that the list is active
        new Handler(getMainLooper()).postDelayed(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED), 1000);

    }

    private void setPolyLine() {
        if (mRideService.getPolyLineOptions() != null) {

            if (mMap != null) {
                if (mCurrentPolyline != null)
                    mCurrentPolyline.remove();
                mCurrentPolyline = mMap.addPolyline((mRideService.getPolyLineOptions()).geodesic(true)
                        .color(Color.RED)
                        .width(12));
                int a;
            } else {
                new Handler(getMainLooper()).postDelayed(() -> setPolyLine(), 1000);// doing this so as to avoid missing polyline if mao was not ready
            }
        }


    }

    @Override
    public void updatePolyline(PolylineOptions currentStopPolylineOptions) {
        if (mMap != null) {
            if (mCurrentPolyline != null)
                mCurrentPolyline.remove();
            mCurrentPolyline = mMap.addPolyline((currentStopPolylineOptions).geodesic(true)
                    .color(Color.RED)
                    .width(12));
        }
    }

    @Override
    public void setDistanceTimeSpeed(String distance, String time, String speed) {

        distanceTextView.setText(distance);
        timeTextView.setText(time);
        speedTextView.setText(speed);
//        mActiveRideArrayList.clear();??? why  would I do that

    }

    @Override
    public void abortJourney() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        isjournyActive = false;
        if (mCurrentPolyline != null)
            mCurrentPolyline.remove();
        mCurrentPolyline = null;
    }
    //
//    @Override
//    public void setBottomSheetState(int state) {
//        bottomSheetBehavior.setState(state);
//    }
//
//    @Override
//    public void hideBottomsheet() {
//        isjournyActive = false;
//        bottomSheetBehavior.setHideable(true);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//    }
//

//    @Override
//    public void removePolyLines() {
//        if (mCurrentPolyline != null) {
//
//            mCurrentPolyline.remove();
//            mCurrentPolyline = null;
//
//        }
//        if (mFullRoutePolyline != null) {
//            mFullRoutePolyline.remove();
//            shouldDrawfullRoute = true;//to rstore the full path drawing for future rides
//        }
//    }
//
//    @Override
//    public void ontaskdone(Object... values) {
//        if ((boolean) values[1]) { //this means its a full route
//            if (mFullRoutePolyline != null) {
//                mFullRoutePolyline.remove();
//            }
//            mFullRoutePolyline = mMap.addPolyline(((PolylineOptions) values[0]).geodesic(true));
//            shouldDrawfullRoute = false;
////                    .color(Color.CYAN)
////                    .width(10))
//
//        } else {
//            if (mCurrentPolyline != null)
//                mCurrentPolyline.remove();
//            mCurrentPolyline = mMap.addPolyline(((PolylineOptions) values[0]).geodesic(true)
//                    .color(Color.RED)
//                    .width(12));
//            /*.pattern(PattrrPATTERN_POLYGON_ALPHA))*/
//            ;
//        }
//    }
}
//// TO DO: 7/27/2019 for the purpose of Gps use a broadcast reciever for making changes as desired