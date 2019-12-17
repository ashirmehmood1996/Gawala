package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Adapters.ConnectedProducersAdapter;
import com.android.example.gawala.Consumer.Adapters.ProducersAdapter;
import com.android.example.gawala.Consumer.Models.ProducerModel;
import com.android.example.gawala.Generel.AsyncTasks.GeoCoderAsyncTask;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;


public class ConsumerRequestsActivity extends AppCompatActivity implements ProducersAdapter.CallBack, ConnectedProducersAdapter.Callback {

    private LinearLayout connectedProducerMainLinearLayout;

    private ArrayList<ProducerModel> connectedProducerArrayList;
    private RecyclerView connectedProducersRecyclerView;
    private ConnectedProducersAdapter connectedProducersAdapter;


    private ArrayList<ProducerModel> producerModelArrayList;
    private ProducersAdapter producersAdapter;
    private RecyclerView allProdcuersrecyclerView;


    private String myId;

    private boolean shouldCall = true;
    private ValueEventListener mConnectedProducersListener;
    private DatabaseReference mConnectedProducerNodeRef;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_requests);


        getSupportActionBar().setTitle("Providers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initFields();
        attachListeners();
        loadConectedProducers();
    }


    private void initFields() {
        initializeDialog();
        connectedProducerMainLinearLayout = findViewById(R.id.ll_con_req_connected_producer);

        //recycler view related
        producerModelArrayList = new ArrayList<>();
        allProdcuersrecyclerView = findViewById(R.id.rv_con_all_producers);
        producersAdapter = new ProducersAdapter(producerModelArrayList, this);
        allProdcuersrecyclerView.setAdapter(producersAdapter);


        connectedProducerArrayList = new ArrayList<>();
        connectedProducersRecyclerView = findViewById(R.id.rv_con_req_conected_consumers);
        connectedProducersAdapter = new ConnectedProducersAdapter(connectedProducerArrayList, this);
        connectedProducersRecyclerView.setAdapter(connectedProducersAdapter);

        //database related
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mConnectedProducerNodeRef = rootRef.child("connected_producers").child(myId);

        mConnectedProducersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                connectedProducerArrayList.clear();
                if (dataSnapshot.exists() && ConsumerRequestsActivity.this != null) {
                    connectedProducerMainLinearLayout.setVisibility(View.VISIBLE);
                    for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                        String key = producerSnap.getKey();
                        String name = producerSnap.child("name").getValue(String.class);
                        String number = producerSnap.child("number").getValue(String.class);
                        String imageUri = "";
                        if (!shouldCall) { // this means that this listener is not executing for the first time and all producers arraylist may be populated
                            for (ProducerModel producerModel : producerModelArrayList) {
                                if (producerModel.getId().equals(key)) {
                                    imageUri = producerModel.getImageUri();
                                }
                            }
                        }
                        String lat = producerSnap.child("location").child("lat").getValue(String.class);
                        String lng = producerSnap.child("location").child("lng").getValue(String.class);
                        ProducerModel producerModel = new ProducerModel(key, name, number, imageUri, lat, lng);
                        producerModel.setStatus(ProducerModel.REQUEST_ACCEPTED);
                        connectedProducerArrayList.add(producerModel);
                    }
                } else {
                    connectedProducerMainLinearLayout.setVisibility(View.GONE);
                }
                connectedProducersAdapter.notifyDataSetChanged();

                if (shouldCall) {
                    fetchCityAndCountryName();
                    shouldCall = false;
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void attachListeners() {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadConectedProducers() {
        initializeDialog();
        mConnectedProducerNodeRef.addValueEventListener(mConnectedProducersListener);

    }

    private void fetchCityAndCountryName() {

        double lat = Double.parseDouble(SharedPreferenceUtil.getValue(getApplicationContext(), "lat"));
        double lng = Double.parseDouble(SharedPreferenceUtil.getValue(getApplicationContext(), "lng"));
        LatLng latLng = new LatLng(lat, lng);
        new GeoCoderAsyncTask(ConsumerRequestsActivity.this) {
            @Override
            protected void onPostExecute(Address address) {
                if (address != null) {
                    String countryName = address.getCountryName();
                    String city = address.getLocality();
                    loadAllProducers(city, countryName);
                }

            }
        }.execute(latLng);

    }

    private void loadAllProducers(String city, String country) {
        mAlertDialog.show();

        //not changing database schema for now and qurying all the data whihc is a bad practice later we can find better apoproaches if time
        //for now  loading all producers later that can be changed when the system expands
        rootRef
                .child("users").orderByChild("type").equalTo(getResources().getString(R.string.provider))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // TODO: 11/16/2019  test by adding more producers
                        if (dataSnapshot.exists() && ConsumerRequestsActivity.this != null) {
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
                                if (!producerSnap.hasChild("cities")) {
                                    continue;
                                }
                                for (DataSnapshot countrySnap : producerSnap.child("cities").getChildren()) {
                                    if (countrySnap.getKey().equals(country)) {
                                        for (DataSnapshot citySnap : countrySnap.getChildren()) {
                                            if (citySnap.getValue(String.class) != null
                                                    && citySnap.getValue(String.class).equals(city)) {
                                                String id = producerSnap.getKey();
                                                String name = producerSnap.child("name").getValue(String.class);
                                                String number = producerSnap.child("number").getValue(String.class);
                                                String imageUri = "";
                                                if (producerSnap.hasChild("profile_image_uri")) {
                                                    imageUri = producerSnap.child("profile_image_uri").getValue(String.class);
                                                }
                                                String lat = producerSnap.child("location").child("lat").getValue(String.class);
                                                String lng = producerSnap.child("location").child("lng").getValue(String.class);
                                                ProducerModel producerModel = new ProducerModel(id, name, number, imageUri, lat, lng);
                                                //// FIXME: 11/19/2019 bad practice
                                                for (ProducerModel connectedProducer : connectedProducerArrayList) {
                                                    if (connectedProducer.getId().equals(producerModel.getId())) {
                                                        if (!producerModel.getImageUri().isEmpty()) {//if this producer has image then feed this iamge to the connected one too
                                                            connectedProducer.setImageUri(producerModel.getImageUri());
                                                            connectedProducersAdapter.notifyDataSetChanged();
                                                        }
                                                        producerModel.setStatus(ProducerModel.REQUEST_ACCEPTED);
                                                    }
                                                }


                                                producerModelArrayList.add(producerModel);

                                            }
                                        }
                                    }
                                }

                            }
                            producersAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplicationContext(), "No Producer Found.. Sorry", Toast.LENGTH_SHORT).show();
                        }
                        mAlertDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mAlertDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onProducerItemClcik(int pos) {
        ProducerModel producerModel = producerModelArrayList.get(pos);

        Intent intent = new Intent(this, ProducerDetailActivty.class);
        intent.putExtra("producer_id", producerModel.getId());
        intent.putExtra("name", producerModel.getName());
        intent.putExtra("number", producerModel.getNumber());
        intent.putExtra("status", producerModel.getStatus());
        intent.putExtra("profile_image_uri", producerModel.getImageUri());
        intent.putExtra("lat", producerModel.getLat());
        intent.putExtra("lng", producerModel.getLng());


        startActivity(intent);

    }

    @Override
    public void onconnectedProducerClick(int pos) {

        ProducerModel producerModel = connectedProducerArrayList.get(pos);
        Intent intent = new Intent(ConsumerRequestsActivity.this, ProducerDetailActivty.class);
        intent.putExtra("producer_id", producerModel.getId());
        intent.putExtra("name", producerModel.getName());
        intent.putExtra("number", producerModel.getNumber());
        intent.putExtra("status", producerModel.getStatus());
        intent.putExtra("profile_image_uri", producerModel.getImageUri());
        intent.putExtra("lat", producerModel.getLat());
        intent.putExtra("lng", producerModel.getLng());
        intent.putExtra("is_connected", true);

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (mConnectedProducersListener != null) {
            mConnectedProducerNodeRef.removeEventListener(mConnectedProducersListener);
        }
        super.onDestroy();
    }
}

