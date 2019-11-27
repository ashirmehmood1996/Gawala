package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ConsumerRequestsActivity extends AppCompatActivity implements ProducersAdapter.CallBack, ConnectedProducersAdapter.Callback {

    private LinearLayout connectedProducerMainLinearLayout;

    private ArrayList<ProducerModel> connectedProducerArrayList;
    private RecyclerView connectedProducersRecyclerView;
    private ConnectedProducersAdapter connectedProducersAdapter;


    private ArrayList<ProducerModel> producerModelArrayList;
    private ProducersAdapter producersAdapter;
    private RecyclerView allProdcuersrecyclerView;


    private DatabaseReference rootRef;
    private String myId;

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
        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        rootRef.child("connected_producers").child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && ConsumerRequestsActivity.this != null) {
                            connectedProducerMainLinearLayout.setVisibility(View.VISIBLE);
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                String key = producerSnap.getKey();
                                String name = producerSnap.child("name").getValue(String.class);
                                String number = producerSnap.child("number").getValue(String.class);
//                                String imageUri="";
//                                for (ProducerModel producerModel:producerModelArrayList){// getting image uri from
//                                    if (producerModel.getId()==key){
//                                        if (!producerModel.getImageUri().isEmpty()){
//                                            imageUri=producerModel.getImageUri();
//                                        }
//                                    }
//                                }

                                ProducerModel producerModel = new ProducerModel(key, name, number, "");
                                producerModel.setStatus(ProducerModel.REQUEST_ACCEPTED);
                                connectedProducerArrayList.add(producerModel);
                            }
                        }

                        connectedProducersAdapter.notifyDataSetChanged();

                        fetchCityAndCountryName();

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

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

        //not changing database schema for now and qurying all the data whihc is a bad practice later we can find better apoproaches if time
        //for now  loading all producers later that can be changed when the system expands
        FirebaseDatabase.getInstance().getReference()
                .child("users").orderByChild("type").equalTo("producer")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // TODO: 11/16/2019  test by adding more producers
                        // TODO: 11/16/2019  only add the producer model if it is providing its service`s in` this area
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
                                                ProducerModel producerModel = new ProducerModel(id, name, number, imageUri);
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "databse error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


//    @Override
//    public void onSendRequest(int pos) {
//        ProducerModel producerModel = producerModelArrayList.get(pos);
//
//        HashMap<String, Object> requestMap = new HashMap<>();
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        requestMap.put("number", currentUser.getPhoneNumber());
//        requestMap.put("name", currentUser.getDisplayName());
//        requestMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");// // TODO: 8/8/2019  later deal with time zones
//        String lat = SharedPreferenceUtil.getValue(getApplicationContext(), "lat");
//        String lng = SharedPreferenceUtil.getValue(getApplicationContext(), "lng");
//        if (lat != null && !lat.isEmpty() && lng != null && !lng.isEmpty()) {
//            requestMap.put("lat",lat);
//            requestMap.put("lng",lng);
//        }else {
//            Toast.makeText(this, "Location is not set.Request cannot be sent. Please set the location in personal Information first. ", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        FirebaseDatabase.getInstance().getReference()
//                .child("requests")
//                .child(producerModel.getId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .setValue(requestMap)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(ConsumerRequestsActivity.this, "request sent now deal with UI too", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(ConsumerRequestsActivity.this, "failed to send request", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }

    @Override
    public void onProducerItemClcik(int pos) {
        ProducerModel producerModel = producerModelArrayList.get(pos);

        Intent intent = new Intent(this, ProducerDetailActivty.class);
        intent.putExtra("producer_id", producerModel.getId());
        intent.putExtra("name", producerModel.getName());
        intent.putExtra("number", producerModel.getNumber());
        intent.putExtra("status", producerModel.getStatus());
        intent.putExtra("profile_image_uri", producerModel.getImageUri());

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

        startActivity(intent);
    }

//// TODO: 10/19/2019 allow to place the location in order to make producer able to accept the request


    // TODO: 6/30/2019  later deal with cancel friend requests
// TODO: 6/30/2019  avoid sending request to a producer thats already connected
}

//do now will take few minutes
// TODO: 10/16/2019  show teh data for send request cancel requesta and remove producer acordingly may be we shuld not add remove producer because of trusta adn business issues
//  WE HAVE ALREADY OROVIDED OPTION TO MAKE ZERO DEMAND THIS IS ENOUGH FOR THE CONSUMER . ALTHOUGH THE PRODUCER WILL BE ABLE TO REMOVE THE CONSUMER so we will remove the send request option button for connected consumers


///send request with latlng alongside, the producer will get the distance by road via distance matrix api and see this client  on map and will decide weather to accept or reject the freind requestr in this same module the new locations will be used  that are in users node i guess and also implement all the buttons functionalities that were left at the time or hurry

