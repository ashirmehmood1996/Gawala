package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Adapters.ConnectedProducersAdapter;
import com.android.example.gawala.Consumer.Adapters.ProducersAdapter;
import com.android.example.gawala.Consumer.Models.ProducerModel;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class ConsumerRequestsActivity extends AppCompatActivity implements ProducersAdapter.CallBack, ConnectedProducersAdapter.Callback {
    //producer item related
//    private TextView producerNameTextView;
//    private TextView producerNumberTextView;
    private LinearLayout connectedProducerMainLinearLayout;
//    private LinearLayout connectedProducerLinearLayout;


    private ArrayList<String> connectedProducersArrayList;
    private RecyclerView connectedProducersRecyclerView;
    private ConnectedProducersAdapter connectedProducersAdapter;
    String producerId;


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
        loadAllProducers();
    }


    private void initFields() {
        connectedProducerMainLinearLayout = findViewById(R.id.ll_con_req_connected_producer);

        //recycler view related
        producerModelArrayList = new ArrayList<>();
        allProdcuersrecyclerView = findViewById(R.id.rv_con_all_producers);
        producersAdapter = new ProducersAdapter(producerModelArrayList, this);
        allProdcuersrecyclerView.setAdapter(producersAdapter);


        connectedProducersArrayList = new ArrayList<>();
        connectedProducersRecyclerView = findViewById(R.id.rv_con_req_conected_consumers);
        connectedProducersAdapter = new ConnectedProducersAdapter(connectedProducersArrayList, this);
        connectedProducersRecyclerView.setAdapter(connectedProducersAdapter);

        //database related
        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void attachListeners() {
    }

    private void loadConectedProducers() {
        // TODO: 10/6/2019  change this qury in order toa avoid whole data fatching  or we can change the database schema
        //lter deal with query
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {

                                        connectedProducersArrayList.add(producerSnap.getKey());

                                        producerId = producerSnap.getKey();//producer key
//                                        String name = producerSnap.child("name").getValue(String.class);
//                                        String number = producerSnap.child("number").getValue(String.class);
                                        connectedProducerMainLinearLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                            connectedProducersAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadAllProducers() {
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
                                producerModelArrayList.add(new ProducerModel(id, name, number));
                            }
                            producersAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ConsumerRequestsActivity.this, "No Producer Found.. Sorry", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
        // FIXME: 10/6/2019 this isa  temporary solution later we have to fetch it in the loadConectedProducers() method
        for (ProducerModel producerModel : producerModelArrayList) {
            if (producerModel.getId().equals(producerId)) {
                Intent intent = new Intent(ConsumerRequestsActivity.this, ProducerDetailActivty.class);
                intent.putExtra("producer_id", producerModel.getId());
                intent.putExtra("name", producerModel.getName());
                intent.putExtra("number", producerModel.getNumber());
                startActivity(intent);
                break;
            }
        }
    }

    // TODO: 6/30/2019  later deal with cancel friend requests
// TODO: 6/30/2019  avoid sending request to a producer thats already connected
}
