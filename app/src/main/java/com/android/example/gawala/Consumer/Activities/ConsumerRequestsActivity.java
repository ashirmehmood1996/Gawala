package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Adapters.ConnectedProducersAdapter;
import com.android.example.gawala.Consumer.Adapters.ProducersAdapter;
import com.android.example.gawala.Consumer.Models.ProducerModel;
import com.android.example.gawala.Producer.Activities.ProducerNavMapActivity;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initFields();
        attachListeners();
        loadConectedProducers();
        ///// FIXME: 10/8/2019 those producers which are not requested should not be showing the add service in demand option rather a hint that says "send request to add this product"
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

    private void loadConectedProducers() {

        rootRef.child("connected_producers").child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            connectedProducerMainLinearLayout.setVisibility(View.VISIBLE);
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                String key = producerSnap.getKey();
                                String name = producerSnap.child("name").getValue(String.class);
                                String number = producerSnap.child("number").getValue(String.class);
                                connectedProducerArrayList.add(new ProducerModel(key, name, number));

                            }
                        }

                        connectedProducersAdapter.notifyDataSetChanged();
                        loadAllProducers();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadAllProducers(String city, String country) {

        either change the database schema or else query all the data and filter here whihc is ofcpurse a bad practice
                compare cloud firestore and shift if necassary later may be
        //for now  loading all producers later that can be changed when the system expands
        FirebaseDatabase.getInstance().getReference()
                .child("users").orderByChild("type").equalTo("producer")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
                                String id = producerSnap.getKey();
                                String name = producerSnap.child("name").getValue(String.class);
                                String number = producerSnap.child("number").getValue(String.class);
                                ProducerModel producerModel = new ProducerModel(id, name, number);


//                                for (ProducerModel producerModel1:connectedProducerArrayList){
//                                    if (producerModel1.equals(producerModel)){
//                                        producerModel.setStatus(ProducerModel.REQUEST_ACCEPTED);
//                                    }
//
//                                }

                                producerModelArrayList.add(producerModel);
                            }
                            producersAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ConsumerRequestsActivity.this, "No Producer Found.. Sorry", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ConsumerRequestsActivity.this, "databse error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onSendRequest(int pos) {
        ProducerModel producerModel = producerModelArrayList.get(pos);

        HashMap<String, Object> requestMap = new HashMap<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        requestMap.put("number", currentUser.getPhoneNumber());
        requestMap.put("name", currentUser.getDisplayName());
        requestMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");// // TODO: 8/8/2019  later deal with time zones

        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(producerModel.getId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(requestMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ConsumerRequestsActivity.this, "request sent now deal with UI too", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(ConsumerRequestsActivity.this, "failed to send request", Toast.LENGTH_SHORT).show();
                        }
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

        startActivity(intent);

    }

    @Override
    public void onconnectedProducerClick(int pos) {

        ProducerModel producerModel = connectedProducerArrayList.get(pos);
        Intent intent = new Intent(ConsumerRequestsActivity.this, ProducerDetailActivty.class);
        intent.putExtra("producer_id", producerModel.getId());
        intent.putExtra("name", producerModel.getName());
        intent.putExtra("number", producerModel.getNumber());
        startActivity(intent);
    }

//// TODO: 10/19/2019 allow to place the location in order to make producer able to accept the request


    // TODO: 6/30/2019  later deal with cancel friend requests
// TODO: 6/30/2019  avoid sending request to a producer thats already connected
}

//do now will take few minutes
// TODO: 10/16/2019  show teh data for send request cancel requesta and remove producer acordingly may be we shuld not add remove producer because of trusta adn business issues
//  WE HAVE ALREADY OROVIDED OPTION TO MAKE ZERO DEMAND THIS IS ENOUGH FOR THE CONSUMER . ALTHOUGH THE PRODUCER WILL BE ABLE TO REMOVE THE CONSUMER so we will remove the send request option button for connected consumers
