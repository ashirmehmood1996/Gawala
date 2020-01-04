package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Constants;
import com.android.example.gawala.Consumer.Models.ProducerModel;
import com.android.example.gawala.Generel.Adapters.GoodsAdapter;
import com.android.example.gawala.Generel.Fraagments.DistanceViewerMapsFragment;
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
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProducerDetailActivty extends AppCompatActivity implements GoodsAdapter.CallBack {

    public static final String EXTRA_ANIMAL_IMAGE_TRANSITION_NAME = "imageTransitionName";
    private String providerId;
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
    private String MAP_TAG = "mapTAG";
    private String lat, lng;
    private boolean isConneted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_detail_activty);
        supportPostponeEnterTransition();//for trasition animation


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initFields();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            String transitionName = getIntent().getStringExtra("transitionName");
            profileCircularImageView.setTransitionName(transitionName);
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.curve));
            supportStartPostponedEnterTransition();
        }


        attachListeners();
        loadThisProducerStatus();
        loadThisProducerGoods();


    }


    private void initFields() {
        Intent intent = getIntent();
        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");
        providerId = getIntent().getStringExtra("producer_id");
        nameTextView = findViewById(R.id.tv_prod_detail_name);
        numberTextView = findViewById(R.id.tv_prod_detail_number);
        profileCircularImageView = findViewById(R.id.civ_prod_detail_picture);
        name = intent.getStringExtra("name");
        nameTextView.setText(name);
        number = intent.getStringExtra("number");
        numberTextView.setText(number);
        imageUri = intent.getStringExtra("profile_image_uri");


        if (!imageUri.isEmpty()) {
            Glide.with(getApplicationContext()).load(imageUri).into(profileCircularImageView);
        }
        status = getIntent().getIntExtra("status", ProducerModel.STATUS_NEUTRAL);
        if (status == ProducerModel.REQUEST_ACCEPTED) {
            isConneted = true;
        }
        requestButton = findViewById(R.id.bt_prod_detail_request);
        seeOnMapButton = findViewById(R.id.bt_prod_detail_see_on_map);

        recyclerView = findViewById(R.id.rv_prod_detail);
        goodModelArrayList = new ArrayList<>();
        goodsAdapter = new GoodsAdapter(goodModelArrayList, this);
        recyclerView.setAdapter(goodsAdapter);


    }

    private void loadThisProducerGoods() {
        rootRef.child("goods").child(providerId).addListenerForSingleValueEvent(new ValueEventListener() {
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
            showOnMap();
//            checkIfProvideIsSharingInfo();
        });
    }

//    private void checkIfProvideIsSharingInfo() {
//        rootRef
//                .child("share_location")
//                .child(providerId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists() && ProducerDetailActivty.this != null) {
//
//                            boolean isShared = dataSnapshot.getValue(Boolean.class);
//                            if (isShared) {
//                                showOnMap();
//                            } else {
//                                Toast.makeText(getApplicationContext(), "this producer is not sharing location at that time", Toast.LENGTH_SHORT).show();
//                            }
//
//                        } else {
//                            Toast.makeText(ProducerDetailActivty.this, "this producer is not sharing location at that time", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
//
//    }

    //    private void showOnMap() {
//        Intent intent = new Intent(this, ConsumerMapActivity.class);
//        intent.putExtra("producer_id", providerId);
//        startActivity(intent);
//    }
    private void showOnMap() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        DistanceViewerMapsFragment clientInfoFullScreenDialogFragment =
                (DistanceViewerMapsFragment) getSupportFragmentManager().findFragmentByTag(MAP_TAG);
        if (clientInfoFullScreenDialogFragment != null) {
            fragmentTransaction.remove(clientInfoFullScreenDialogFragment).commit();
        }

        DistanceViewerMapsFragment dialogFragment =
                DistanceViewerMapsFragment.newInstance(Double.parseDouble(lat),
                        Double.parseDouble(lng), "Provider");
//        dialogFragment.setCallback(this);
        dialogFragment.show(fragmentTransaction, MAP_TAG);
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

        rootRef.child("requests")
                .child(providerId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(requestMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (ProducerDetailActivty.this != null) {
                            if (task.isSuccessful()) {
                                requestButton.setText("cancel Request");
                                status = ProducerModel.REQUEST_SENT;
                                sendNotification();
                                Toast.makeText(getApplicationContext(), "request sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "failed to send request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    /**
     * this method is responsible to notify the respective provider that request is sent
     */
    private void sendNotification() {
        String message = "You have a new Request from " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", "New Request");
        notificationMap.put("message", message);
        notificationMap.put("type", Constants.Notification.TYPE_NEW_REQUEST);
        notificationMap.put("sender_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        notificationMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");

//        for (int i = 0; i < 100; i++) {
//            String newtitle = title + "  " + i;
//            notificationMap.put("title", newtitle);
        rootRef.child("notifications")
                .child(providerId)//reciever id
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//sender id
                .push()//notification id
                .setValue(notificationMap);//for now no need for completion listener
//        }
    }

    private void cancelRequest() {
        rootRef
                .child("requests")
                .child(providerId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
            requestButton.setText("Remove");
            requestButton.setEnabled(true);
            return;
        }
        //listen if the request is already sent
        rootRef.child("requests")
                .child(providerId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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
    public void onGoodItemClick(int position, ImageView sharedImageView) {
        GoodModel currentModel = goodModelArrayList.get(position);
        Intent intent = new Intent(this, ConProducerServiceDetailsActivty.class);
        intent.putExtra("goods_model", currentModel);
        intent.putExtra("producer_id", providerId);
        intent.putExtra("is_connected", isConneted);
        intent.putExtra(EXTRA_ANIMAL_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedImageView,
                ViewCompat.getTransitionName(sharedImageView));
        startActivity(intent, options.toBundle());
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