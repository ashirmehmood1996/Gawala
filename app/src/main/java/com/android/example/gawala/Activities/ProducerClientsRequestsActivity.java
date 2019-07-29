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

        //// TODO: 7/27/2019 need runtime permissions for accessing fine locations
        // TODO: 7/27/2019 ur on GPS if not active
        //// TODO: 7/27/2019 for the purpose of Gps use a broadcast reciever for making changes as desired
        //now i am able to ge the location now further take the next lessonss
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
                Toast.makeText(ProducerClientsRequestsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                requestLocationUpdates();

            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Toast.makeText(ProducerClientsRequestsActivity.this, "resolvable failure", Toast.LENGTH_SHORT).show();
//               try {
//                   // Show the dialog by calling startResolutionForResult(),
//                   // and check the result in onActivityResult().
////                   ResolvableApiException resolvable = (ResolvableApiException) e;
////                   resolvable.startResolutionForResult(ProducerClientsRequestsActivity.this,
////                           REQUEST_CHECK_SETTINGS);
//               } catch (IntentSender.SendIntentException sendEx) {
//                   // Ignore the error.
//               }

                } else {
                    Toast.makeText(ProducerClientsRequestsActivity.this, "non resolvable failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        //final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "have permissions", Toast.LENGTH_SHORT).show();
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Toast.makeText(ProducerClientsRequestsActivity.this, "", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("locationUpdaes")
                            .push().setValue(locationResult).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProducerClientsRequestsActivity.this, "node updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProducerClientsRequestsActivity.this, "error updatinf firebase", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
//                    Location location = locationResult.getLastLocation();
//                    if (location != null) {
//                        Log.d(TAG, "location update " + location);
//                        ref.setValue(location);
//                    }
                }
            }, null);
        } else {
            Toast.makeText(this, "no permissions", Toast.LENGTH_SHORT).show();
        }
    }
}
