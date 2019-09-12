package com.android.example.gawala.Activities;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Adapters.ConnectedConsumersAdapter;
import com.android.example.gawala.Adapters.RequestsAdapter;
import com.android.example.gawala.Interfaces.RequestsAdapterCallbacks;
import com.android.example.gawala.Models.ConnectedConsumersModel;
import com.android.example.gawala.Models.RequestModel;
import com.android.example.gawala.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ProducerClientsRequestsActivity extends AppCompatActivity implements RequestsAdapterCallbacks {

    private TextView newRequestsTitleTextView;
    private RecyclerView requestsRecyclerView;
    private ArrayList<RequestModel> requestModelArrayList;
    private RequestsAdapter requestsAdapter;

    private RecyclerView connectedConsumersRecyclerView;
    private ArrayList<ConnectedConsumersModel> consumersArrayList;
    private ConnectedConsumersAdapter consumersAdapter;


    private DatabaseReference rootRef;
    private String myID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initfields();

        loadRequests();
        loadConnectedConsumers();


    }


    private void initfields() {
        //dataBase related
        rootRef = FirebaseDatabase.getInstance().getReference();
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //requests related
        newRequestsTitleTextView = findViewById(R.id.tv_pro_new_requests);
        requestsRecyclerView = findViewById(R.id.rv_pro_request);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestModelArrayList = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(requestModelArrayList, this);
        requestsRecyclerView.setAdapter(requestsAdapter);

        //connected consumers related
        connectedConsumersRecyclerView = findViewById(R.id.rv_pro_consumers);
        connectedConsumersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        consumersArrayList = new ArrayList<>();
        consumersAdapter = new ConnectedConsumersAdapter(consumersArrayList, this);
        connectedConsumersRecyclerView.setAdapter(consumersAdapter);


    }


    private void loadRequests() {
        requestModelArrayList.clear();
        rootRef.child("requests").child(myID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {//then there are a number of requests
                            for (DataSnapshot requestSnap : dataSnapshot.getChildren()) {
                                String senderID = requestSnap.getKey();


                                String name = requestSnap.child("name").getValue(String.class);
                                String number = requestSnap.child("number").getValue(String.class);
                                String timeStamp = requestSnap.child("time_stamp").getValue(String.class);
                                requestModelArrayList.add(new RequestModel(senderID, name, number, timeStamp, null, null));
                            }
                            requestsAdapter.notifyDataSetChanged();
                        } else {
                            newRequestsTitleTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadConnectedConsumers() {

        consumersArrayList.clear();
        rootRef.child("clients").child(myID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot consumerSnapshot : dataSnapshot.getChildren()) {
                                String consumerKey = consumerSnapshot.getKey();

                                String number = consumerSnapshot.child("number").getValue(String.class);
                                String name = consumerSnapshot.child("name").getValue(String.class);
                                consumersArrayList.add(new ConnectedConsumersModel(consumerKey, name, number, null, null));
                            }

                        }
                        consumersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public void onRequestCancel(int position) {
        final RequestModel requestModel = requestModelArrayList.get(position);
        removeRequestNode(requestModel.getSender_id(), false, position);

    }

    @Override
    public void onRequestAccepted(final int position) {
        final RequestModel requestModel = requestModelArrayList.get(position);


        HashMap<String, Object> clientMap = new HashMap<>();

        String name = requestModel.getName();
        String number = requestModel.getNumber();
        String time = requestModel.getTime_stamp();
        clientMap.put("name", name);
        clientMap.put("number", number);
        //clientMap.put("time_stamp",time); //this time stamp is the time of sending this request
        String time_accept = Calendar.getInstance().getTimeInMillis() + "";
        clientMap.put("time_stamp", time_accept);
        FirebaseDatabase.getInstance().getReference()
                .child("clients").child(myID)
                .child(requestModel.getSender_id())
                .setValue(clientMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            removeRequestNode(requestModel.getSender_id(), true, position);
                        } else {
                            Toast.makeText(ProducerClientsRequestsActivity.this, "something went wrong try later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void removeRequestNode(String sender_id, final boolean isAccepted, final int position) {
        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(myID)
                .child(sender_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (isAccepted) {
                        Toast.makeText(ProducerClientsRequestsActivity.this, "Client added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProducerClientsRequestsActivity.this, "request removed successfully", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(ProducerClientsRequestsActivity.this, "something went wrong try later", Toast.LENGTH_SHORT).show();
                }
                requestModelArrayList.remove(position);
                requestsAdapter.notifyItemRemoved(position);
                requestsAdapter.notifyItemRemoved(position);
                requestsAdapter.notifyItemRangeChanged(position, requestModelArrayList.size());
                loadConnectedConsumers();
            }
        });
    }
}
//todo later divide user info in pulic and private pulis is demnded by any user but private only by the current user

//create a fragment that will be a dashboard for producer where todays demand of milk and other sttats will be shown