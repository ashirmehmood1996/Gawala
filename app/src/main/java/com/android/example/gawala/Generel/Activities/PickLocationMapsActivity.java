package com.android.example.gawala.Generel.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.example.gawala.Generel.AsyncTasks.GeoCoderAsyncTask;
import com.android.example.gawala.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class PickLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String from;
    private static final int RC_PERMISSION_ALL = 1212;
    private static final int RC_LOCAION_ON = 1211;
    private GoogleMap mMap;
    private Button proceedButton, cancelButton;
    private ImageView markerImageView, dotImageView;
    private CameraPosition mPosition;
    private LatLng mcurrentLocation;
    //places related
    //took help from this link https://www.youtube.com/watch?v=jtOYctzpa_w and this official doc link https://developers.google.com/places/android-sdk/autocomplete#add_an_autocomplete_widget
    private PlacesClient mPlacesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.VIEWPORT);//places api takes a lisr of data field that we require from the query
    private AutocompleteSupportFragment placesFragment;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    private GeoCoderAsyncTask mGeocoderAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location_maps);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!hasPermissions(this, PERMISSIONS)) {
            requestAllPermissions();
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlerDialogForGPS();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initfields();
        attachListeners();
        initPlaces();
        setUpPlacesAutoComplete();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
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

    private void attachListeners() {
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (from.equals(getResources().getString(R.string.from_consumer_dash_board))) {
                Intent intent = new Intent();
                intent.putExtra("lat", mcurrentLocation.latitude);
                intent.putExtra("lng", mcurrentLocation.longitude);
                setResult(RESULT_OK, intent);

                if (!mGeocoderAsyncTask.isCancelled()) {
                    mGeocoderAsyncTask.cancel(true);
                }

                finish();
//                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelEditLocationAlertDialog();
            }
        });
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
                                ActivityCompat.requestPermissions(PickLocationMapsActivity.this, PERMISSIONS,
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
                        Toast.makeText(PickLocationMapsActivity.this, "location is not enabled app will shut down shortly", Toast.LENGTH_SHORT).show();
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
                handler.postDelayed(() -> finish(), 2000);
            }
        }

        //      donot allow the onmap raedy proceed unless the permissions are granted and gps is on
//
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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


    private void initPlaces() {
        Places.initialize(this, getResources().getString(R.string.places_api_key));
        mPlacesClient = Places.createClient(this);
    }

    private void setUpPlacesAutoComplete() {
        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.f_pick_location_map_autocomplete);
        placesFragment.setPlaceFields(placeFields);
//        placesFragment.setTypeFilter();
        placesFragment.setHint("Search Location");
        placesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                Toast.makeText(PickLocationMapsActivity.this, "name: ", Toast.LENGTH_SHORT).show();
//                mMap.setLatLngBoundsForCameraTarget(place.getViewport());
//                place.

//                if ( place.getViewport() != null) {
//                    Polyline polyline1 = mMap.addPolyline((new PolylineOptions())
//                            .clickable(true)
//                            .add(place.getViewport().northeast, place.getViewport().southwest, place.getViewport().getCenter(), place.getLatLng()));
//                }
//                Polygon polygon1 = mMap.addPolygon(new PolygonOptions()
//                        .clickable(true)
//                        .add(
//                                /*new LatLng(-27.457, 153.040),
//                                new LatLng(-33.852, 151.211),
//                                new LatLng(-37.813, 144.962),
//                                new LatLng(-34.928, 138.599)*/));
//// Store a data object with the polygon, used here to indicate an arbitrary type.
//                polygon1.setTag("alpha");
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 20));
            }

            @Override
            public void onError(@NonNull Status status) {

                Toast.makeText(PickLocationMapsActivity.this, "error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void initfields() {
//        from = getIntent().getStringExtra(getResources().getString(R.string.from_which_activty));
        if (getIntent().hasExtra("lat")) {
            double lat = getIntent().getDoubleExtra("lat", 0);
            double lng = getIntent().getDoubleExtra("lng", 0);
            mcurrentLocation = new LatLng(lat, lng);
        }

        proceedButton = findViewById(R.id.bt_pick_location_map_confirm);
        cancelButton = findViewById(R.id.bt_pick_location_map_cancel);
        markerImageView = findViewById(R.id.iv_pick_location_map_pick_marker);
        dotImageView = findViewById(R.id.iv_pick_location_map_centre_dot);

        mGeocoderAsyncTask = new GeoCoderAsyncTask(PickLocationMapsActivity.this) {
            @Override
            protected void onPostExecute(Address address) {


                placesFragment.setText(address.getAddressLine(0));
            }
        };

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

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                dotImageView.setVisibility(View.VISIBLE);
                markerImageView.startAnimation(AnimationUtils.loadAnimation(PickLocationMapsActivity.this, R.anim.scale_zoom_out));
                markerImageView.setVisibility(View.GONE);

            }
        });
        mMap.setOnCameraIdleListener(() -> {
            mPosition = mMap.getCameraPosition();
            mcurrentLocation = mPosition.target;
            mGeocoderAsyncTask = new GeoCoderAsyncTask(PickLocationMapsActivity.this) {
                @Override
                protected void onPostExecute(Address address) {
                    if (placesFragment != null && address != null) {
                        placesFragment.setText(address.getAddressLine(0));
                    }
                }
            };
            mGeocoderAsyncTask.execute(mcurrentLocation);

            dotImageView.setVisibility(View.INVISIBLE);
            markerImageView.startAnimation(AnimationUtils.loadAnimation(PickLocationMapsActivity.this, R.anim.scale_zoom_in));
            markerImageView.setVisibility(View.VISIBLE);
//                mMap.addMarker(new MarkerOptions().title("new marker").position(mPosition.target));
        });


//        mMap.setMyLocationEnabled(true);// deal later as it is changing the ceter of camera and hence the whole logic


        if (mcurrentLocation == null) {
            animateUserLocation(false);
        } else {
            animateUserLocation(true);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setPadding(0, 250, 0, 0); //todo this property may be pixed dependednt find a fix later

    }

    private void animateUserLocation(boolean hasPreviousLocation) {

        if (hasPreviousLocation) {
            new GeoCoderAsyncTask(PickLocationMapsActivity.this) {
                @Override
                protected void onPostExecute(Address address) {
                    placesFragment.setText(address.getAddressLine(0));
                }
            }.execute(mcurrentLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mcurrentLocation, 16.0f));


        } else {

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
                            if (location != null && PickLocationMapsActivity.this != null) {
                                mcurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                new GeoCoderAsyncTask(PickLocationMapsActivity.this) {
                                    @Override
                                    protected void onPostExecute(Address address) {
                                        if (PickLocationMapsActivity.this != null && address != null)
                                            placesFragment.setText(address.getAddressLine(0));
                                    }
                                }.execute(mcurrentLocation);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mcurrentLocation, 16.0f));


//                            Toast.makeText(MapsActivity.this, "FusedLocationProviderClient Last known Location and provider is " + location.getProvider() + "\n" + "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        }

    }

    private void showCancelEditLocationAlertDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setTitle("CANCEL?")
                .setMessage("are you sure to cancel editing? No progress will be saved")
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Stay Here", null).show();
    }
}
//let the user set up the area of delivery by typing cities names and this may be asked at the time the time when the main activty is startted and leter we can set things up to a new angle
//after that we shall let the user upload pictures
//then we shall keep the integrity of locaton and then UI i guess