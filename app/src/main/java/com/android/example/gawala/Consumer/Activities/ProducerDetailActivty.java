package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Models.ProducerModel;
import com.android.example.gawala.Generel.Adapters.GoodsAdapter;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ProducerDetailActivty extends AppCompatActivity implements GoodsAdapter.CallBack {

    private String producerID;
    private int status;

    private TextView nameTextView, numberTextView;

    private RecyclerView recyclerView;
    private ArrayList<GoodModel> goodModelArrayList;
    private GoodsAdapter goodsAdapter;
    private Button requestButton;
    private Button seeOnMapButton;
    private String name;
    private String number;
    private String imageUri;
    private CircularImageView profileCircularImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_detail_activty);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initFields();
        attachListeners();
        loadThisProducerStatus();
        loadThisProducerGoods();


    }


    private void initFields() {
        Intent intent=getIntent();
        producerID = getIntent().getStringExtra("producer_id");
        nameTextView = findViewById(R.id.tv_prod_detail_name);
        numberTextView = findViewById(R.id.tv_prod_detail_number);
        profileCircularImageView=findViewById(R.id.civ_prod_detail_picture);
        name = intent.getStringExtra("name");
        nameTextView.setText(name);
        number = intent.getStringExtra("number");
        numberTextView.setText(number);
        imageUri=intent.getStringExtra("profile_image_uri");


        if (!imageUri.isEmpty()){
            Glide.with(getApplicationContext()).load(imageUri).into(profileCircularImageView);
        }
        status = getIntent().getIntExtra("status", ProducerModel.STATUS_NEUTRAL);

        requestButton = findViewById(R.id.bt_prod_detail_request);
        seeOnMapButton = findViewById(R.id.bt_prod_detail_see_on_map);

        recyclerView = findViewById(R.id.rv_prod_detail);
        goodModelArrayList = new ArrayList<>();
        goodsAdapter = new GoodsAdapter(goodModelArrayList, this);
        recyclerView.setAdapter(goodsAdapter);


    }

    private void loadThisProducerGoods() {
        FirebaseDatabase.getInstance().getReference()
                .child("goods").child(producerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && ProducerDetailActivty.this != null) {
                    for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
                        GoodModel goodModel = goodSnap.getValue(GoodModel.class);
                        goodModelArrayList.add(goodModel);
                    }
                    goodsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ProducerDetailActivty.this, "this producer has listed no services at the time", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void attachListeners() {
        requestButton.setOnClickListener(v -> {

            if (status == ProducerModel.REQUEST_ACCEPTED) { // then this producer is already in the list
                removeProducer();

            } else if (status == ProducerModel.REQUEST_SENT) { // then this producer is in request list
                cancelRequest();


            } else if (status == ProducerModel.STATUS_NEUTRAL) {// then this producer is new guy to which we can send requests
                sendRequest();

            }

        });
        seeOnMapButton.setOnClickListener(v -> {
            checkIfProvideIsSharingInfo();

        });
    }

    private void checkIfProvideIsSharingInfo() {
        FirebaseDatabase.getInstance().getReference()
                .child("share_location")
                .child(producerID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && ProducerDetailActivty.this != null) {

                            boolean isShared = dataSnapshot.getValue(Boolean.class);
                            if (isShared) {
                                showOnMap();
                            } else {
                                Toast.makeText(getApplicationContext(), "this producer is not sharing location at that time", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(ProducerDetailActivty.this, "this producer is not sharing location at that time", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void showOnMap() {
        Intent intent = new Intent(this, ConsumerMapActivity.class);
        intent.putExtra("producer_id", producerID);
        startActivity(intent);
    }

    private void removeProducer() {
        Toast.makeText(this, "cannot remove the producer right now, you will have to go through a procedure " +
                "", Toast.LENGTH_LONG).show();
    }

    private void sendRequest() {
//        ProducerModel producerModel = producerModelArrayList.get(pos);

        HashMap<String, Object> requestMap = new HashMap<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        requestMap.put("number", currentUser.getPhoneNumber());
        requestMap.put("name", currentUser.getDisplayName());
        requestMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");// // TODO: 8/8/2019  later deal with time zones
        Uri profileImageUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        if (profileImageUri != null) {
            requestMap.put("profile_image_uri", String.valueOf(profileImageUri));
        }
        String lat = SharedPreferenceUtil.getValue(getApplicationContext(), "lat");
        String lng = SharedPreferenceUtil.getValue(getApplicationContext(), "lng");
        if (lat != null && !lat.isEmpty() && lng != null && !lng.isEmpty()) {
            requestMap.put("lat", lat);
            requestMap.put("lng", lng);
        } else {
            Toast.makeText(this, "Location is not set.Request cannot be sent. Please set the location in personal Information first. ", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(producerID).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(requestMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (ProducerDetailActivty.this != null) {
                            if (task.isSuccessful()) {
                                requestButton.setText("cancel Request");
                                status = ProducerModel.STATUS_NEUTRAL;
                                Toast.makeText(getApplicationContext(), "request sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "failed to send request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    private void cancelRequest() {
        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(producerID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful() && ProducerDetailActivty.this != null) {
                    requestButton.setText("send request");
                    status = ProducerModel.STATUS_NEUTRAL;
                }
            }
        });
    }


    private void loadThisProducerStatus() {
        if (status == ProducerModel.REQUEST_ACCEPTED) {
            //then no need to query and we just make the button enabled and also change the text
            requestButton.setText("remove Producer");
            requestButton.setEnabled(true);
            return;
        }
        //listen if the request is already sent
        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(producerID).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (ProducerDetailActivty.this != null) {
                            if (dataSnapshot.exists()) {
                                status = ProducerModel.REQUEST_SENT;
                                requestButton.setText("cancel request");
                            } else {
                                status = ProducerModel.STATUS_NEUTRAL;
                                requestButton.setText("send request");

                            }
                            requestButton.setEnabled(true);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "error: in fetching the producer status ", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onGoodItemClick(int position) {
        GoodModel currentModel = goodModelArrayList.get(position);
        Intent intent = new Intent(this, ConProducerServiceDetailsActivty.class);
        intent.putExtra("goods_model", currentModel);
        intent.putExtra("producer_id", producerID);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);

        }
    }
}