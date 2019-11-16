package com.android.example.gawala.Generel.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Activities.ConsumerDashBoardActivity;
import com.android.example.gawala.Generel.AsyncTasks.GeoCoderAsyncTask;
import com.android.example.gawala.Producer.Fragments.FullScreenEditCitiesFragment;
import com.android.example.gawala.Producer.Models.CityModel;
import com.android.example.gawala.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class PersonalInfoActivity extends AppCompatActivity implements FullScreenEditCitiesFragment.Callback {
    private static final int RC_SET_DELIVERY_LOCATION = 121;
    private static final String DIALOG_EDIT_CITIES = "dialogFragment";
    private TextView nameTextView, numberTextView, locationTextView, typeTextView;
    private ImageButton editLocationImageButton;
    private DatabaseReference rootRef;
    private String myId;

    private LinearLayout deliveryAreasContainerLinearLayout;
    private TextView deliveryAreasTextView;
    private ImageButton editDeliverAreasImageButtons;

    //data
    private String name, number, locatioName, type;
    private LatLng latLng;
    private AlertDialog mAlertDialog;

    private ArrayList<CityModel> cityModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        initFields();
        attachListeners();
        loadDataFromFriebase();
    }

    private void initFields() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nameTextView = findViewById(R.id.tv_personal_info_name);
        numberTextView = findViewById(R.id.tv_personal_info_number);
        locationTextView = findViewById(R.id.tv_personal_info_location_name);
        typeTextView = findViewById(R.id.tv_personal_info_user_type);
        editLocationImageButton = findViewById(R.id.ib_personal_info_edit_location);

        deliveryAreasContainerLinearLayout = findViewById(R.id.ll_personal_info_delivery_areas_container);
        deliveryAreasTextView = findViewById(R.id.tv_personal_info_delivery_areas);
        editDeliverAreasImageButtons = findViewById(R.id.ib_personal_info_edit_delivery_areas);

        cityModelArrayList = new ArrayList<>();
    }

    private void attachListeners() {
        editLocationImageButton.setOnClickListener(v -> showDialogforEditLocation());

        editDeliverAreasImageButtons.setOnClickListener(v -> {
            showEditDeliveryAreasDialog();
        });

    }

    private void showEditDeliveryAreasDialog() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FullScreenEditCitiesFragment dialogFragmentPrev = (FullScreenEditCitiesFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_EDIT_CITIES);
        if (dialogFragmentPrev != null) {
            fragmentTransaction.remove(dialogFragmentPrev);
        }
        FullScreenEditCitiesFragment dialogFragment = FullScreenEditCitiesFragment.getInmstance();
        dialogFragment.setCallback(this);
        dialogFragment.show(fragmentTransaction, DIALOG_EDIT_CITIES);
    }

    private void showDialogforEditLocation() {
        new AlertDialog.Builder(this)
                .setTitle("Edit Location")
                .setMessage("Specify your location on map, Tap Ok to continue..")
                .setPositiveButton("OK", (dialog, which) -> sendToPickLocationActivity()).setNegativeButton("CANCEL", null).show();
    }


    private void sendToPickLocationActivity() {
        Intent intent = new Intent(this, PickLocationMapsActivity.class);
        if (latLng != null) {
            intent.putExtra("lat", latLng.latitude);
            intent.putExtra("lng", latLng.longitude);
        }
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
                    latLng = new LatLng(lat, lng);
                    new GeoCoderAsyncTask(PersonalInfoActivity.this) {
                        @Override
                        protected void onPostExecute(Address address) {
                            locatioName = address.getAddressLine(0);
                            // TODO: 10/29/2019 later animate the layout changes
                            locationTextView.setText(locatioName);
                        }
                    }.execute(latLng);
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
                            Toast.makeText(PersonalInfoActivity.this, "Delivery Location set Successfully", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(PersonalInfoActivity.this, "unable to set Delivery Location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void loadDataFromFriebase() {
        initializeDialog();
        this.mAlertDialog.show();


        rootRef.child("users").child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    name = dataSnapshot.child("name").getValue(String.class);
                    nameTextView.setText(name);
                    number = dataSnapshot.child("number").getValue(String.class);
                    numberTextView.setText(number);
                    type = dataSnapshot.child("type").getValue(String.class);
                    typeTextView.setText(type);

                    if (type.equals("producer")) {
                        deliveryAreasContainerLinearLayout.setVisibility(View.VISIBLE);
                        if (dataSnapshot.hasChild("cities")) {
                            for (DataSnapshot countrySnap : dataSnapshot.child("cities").getChildren()) {
                                String countryName = countrySnap.getKey();
                                for (DataSnapshot citySnap : countrySnap.getChildren()) {
                                    String cityName = citySnap.getValue(String.class);
                                    String id = citySnap.getKey();
                                    CityModel cityModel = new CityModel(id, countryName, cityName);
                                    cityModelArrayList.add(cityModel);
                                }
                            }

                            deliveryAreasTextView.setText("");
                            for (CityModel cityModel : cityModelArrayList) {
                                deliveryAreasTextView.append(String.format("%s, %s\n", cityModel.getCity(), cityModel.getCountry()));
                            }

                        } else {
                            deliveryAreasTextView.setText("Please Specify the delivery Areas, tap on Edit button");
                            deliveryAreasTextView.setTextColor(Color.RED);
                        }

                    }
                    if (dataSnapshot.hasChild("location")) {
                        double lat = Double.parseDouble(dataSnapshot.child("location").child("lat").getValue(String.class));
                        double lng = Double.parseDouble(dataSnapshot.child("location").child("lng").getValue(String.class));
                        locationTextView.setText("fetching location...");
                        locationTextView.setText("fetching location...");
                        latLng = new LatLng(lat, lng);
                        new GeoCoderAsyncTask(PersonalInfoActivity.this) {
                            @Override
                            protected void onPostExecute(Address address) {
                                locatioName = address.getAddressLine(0);;
                                locationTextView.setText(locatioName);
                                mAlertDialog.dismiss();
                            }
                        }.execute(latLng);
                    } else {
                        mAlertDialog.dismiss();
                        Toast.makeText(PersonalInfoActivity.this, "location was not set", Toast.LENGTH_SHORT).show();
                        locationTextView.setText("Warning ! Location was not provided");
                    }

                } else {
                    Toast.makeText(PersonalInfoActivity.this, "Something went wrong. Please restart the application", Toast.LENGTH_SHORT).show();
                    finish();
                    mAlertDialog.dismiss();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PersonalInfoActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mAlertDialog.dismiss();
            }
        });
    }


    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    @Override
    public void onArrayListUpdated(ArrayList<CityModel> arrayList) {
        this.cityModelArrayList = arrayList;
        deliveryAreasTextView.setText("");
        for (CityModel cityModel : cityModelArrayList) {
            deliveryAreasTextView.append(String.format("%s, %s\n", cityModel.getCity(), cityModel.getCountry()));
        }
    }
}