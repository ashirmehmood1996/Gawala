package com.android.example.gawala.Activities;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentSender;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Adapters.ConnectedConsumersAdapter;
import com.android.example.gawala.Adapters.RequestsAdapter;
import com.android.example.gawala.Interfaces.RequestsAdapterCallbacks;
import com.android.example.gawala.Models.ConnectedConsumersModel;
import com.android.example.gawala.Models.RequestModel;
import com.android.example.gawala.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
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

public class ProducerActivity extends AppCompatActivity implements RequestsAdapterCallbacks {

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

        initfields();
        loadRequests();

        loadConnectedConsumers();

        //// TODO: 7/27/2019 need runtime permissions for accessing fine locations
        createLocationRequest();
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
                                String number = requestSnap.getValue(String.class);//later add more fileds to requests
                                requestModelArrayList.add(new RequestModel(senderID, number, null, null, null));
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
                                String number = consumerSnapshot.getValue(String.class);
                                consumersArrayList.add(new ConnectedConsumersModel(consumerKey, number, null, null));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.producer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.nav_producer_logout) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProducerActivity.this, "logout successfull", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ProducerActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "already logged out", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.nav_producer_share_id) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Service.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "id copied to clipboard", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestCancel(int position) {
        final RequestModel requestModel = requestModelArrayList.get(position);
        removeRequestNode(requestModel.getSender_id(), false, position);

    }

    @Override
    public void onRequestAccepted(final int position) {
        final RequestModel requestModel = requestModelArrayList.get(position);


        FirebaseDatabase.getInstance().getReference()
                .child("clients").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(requestModel.getSender_id())
                .setValue(requestModel.getName())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            removeRequestNode(requestModel.getSender_id(), true, position);
                        } else {
                            Toast.makeText(ProducerActivity.this, "something went wrong try later", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ProducerActivity.this, "Client added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProducerActivity.this, "request removed successfully", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(ProducerActivity.this, "something went wrong try later", Toast.LENGTH_SHORT).show();
                }
                requestModelArrayList.remove(position);
                requestsAdapter.notifyItemRemoved(position);
                requestsAdapter.notifyItemRemoved(position);
                requestsAdapter.notifyItemRangeChanged(position, requestModelArrayList.size());
                loadConnectedConsumers();
            }
        });
    }

    //location related
    protected void createLocationRequest() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(ProducerActivity.this, "Success", Toast.LENGTH_SHORT).show();

            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Toast.makeText(ProducerActivity.this, "resolvable failure", Toast.LENGTH_SHORT).show();
//               try {
//                   // Show the dialog by calling startResolutionForResult(),
//                   // and check the result in onActivityResult().
////                   ResolvableApiException resolvable = (ResolvableApiException) e;
////                   resolvable.startResolutionForResult(ProducerActivity.this,
////                           REQUEST_CHECK_SETTINGS);
//               } catch (IntentSender.SendIntentException sendEx) {
//                   // Ignore the error.
//               }
                } else {
                    Toast.makeText(ProducerActivity.this, "non resolvable failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
