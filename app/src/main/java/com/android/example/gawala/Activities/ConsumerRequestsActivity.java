package com.android.example.gawala.Activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class ConsumerRequestsActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText searchEditText;
    private ImageButton searchImageButon;
    private LinearLayout searchResultContainer;
    private TextView searchResultTextView;
    private Button sendRequestButton;
    private String selectedProducerID = null;

    //producer item related
    private LinearLayout producerItemConatiner;
    private TextView producerNameTextView;
    private TextView producerOccupationTextView;


    private DatabaseReference rootRef;
    private String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initFields();
        attachListeners();
        loadConectedProducers();
    }


    private void initFields() {
        searchEditText = findViewById(R.id.et_con_search);
        searchImageButon = findViewById(R.id.ib_con_search);
        searchResultContainer = findViewById(R.id.ll_con_result_container);
        searchResultTextView = findViewById(R.id.tv_con_search_result);
        sendRequestButton = findViewById(R.id.bt_con_send_request);

        //producer related
        producerItemConatiner = findViewById(R.id.ll_con_prod_container);
        producerNameTextView = findViewById(R.id.tv_con_prod_name);
        producerOccupationTextView = findViewById(R.id.tv_con_prod_name);


        //database related

        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void attachListeners() {
        searchImageButon.setOnClickListener(this);
        sendRequestButton.setOnClickListener(this);
    }


    private void loadConectedProducers() {
        //lter deal with query
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {
                                        String key = producerSnap.getKey();//producer key

                                        producerItemConatiner.setVisibility(View.VISIBLE);
                                        producerNameTextView.setText(key);


                                        break;
                                    }

                                }

                            }

                            // TODO: 7/14/2019  later change the query along with the change in data
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_con_search:
                String producerID = searchEditText.getText().toString().trim();

                if (!producerID.isEmpty()) {
                    fetchProducer(producerID);

                } else {
                    Toast.makeText(this, "please fill out the search field", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.bt_con_send_request:

                sendRequestToProducer(v);
                break;
        }
    }


    private void fetchProducer(String producerID) {
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(producerID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (!dataSnapshot.child("type").getValue(String.class).equals("producer")) {
                                Toast.makeText(ConsumerRequestsActivity.this, "no producer found with this id", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            displayProducerInfo(dataSnapshot);
                            Toast.makeText(ConsumerRequestsActivity.this, "producer found", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(ConsumerRequestsActivity.this, "no producer found with this id", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void displayProducerInfo(DataSnapshot producerSnap) {

        if (!searchResultContainer.isShown())
            searchResultContainer.setVisibility(View.VISIBLE);
        if (producerSnap == null) {
            searchResultTextView.setText("no results found");
            sendRequestButton.setEnabled(false);
            selectedProducerID = null;
            return;
        } else {
            sendRequestButton.setEnabled(true);
        }
        String key = producerSnap.getKey();
        String number = producerSnap.child("number").getValue(String.class);
        String type = producerSnap.child("type").getValue(String.class);
        String result = "key : " + key + "\n"
                + "number : " + number + "\n"
                + "type : " + type + "\n";
        selectedProducerID = key;
        searchResultTextView.setText(result);
    }

    private void sendRequestToProducer(final View v) {
        if (selectedProducerID == null) {
            Toast.makeText(this, "user is invaid", Toast.LENGTH_SHORT).show();
            return;
        }


        HashMap<String, Object> requestMap = new HashMap<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        requestMap.put("number", currentUser.getPhoneNumber());
        requestMap.put("name", currentUser.getDisplayName());
        requestMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");// // TODO: 8/8/2019  later deal with time zones
        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(selectedProducerID).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(requestMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ConsumerRequestsActivity.this, "request sent now deal with UI too", Toast.LENGTH_SHORT).show();

                            ((Button) v).setText("request sent");// FIXME: 8/8/2019 later deal with it more polietly
                            v.setEnabled(false);

                        } else {
                            Toast.makeText(ConsumerRequestsActivity.this, "failed to send request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

// TODO: 6/30/2019  later deal with cancel friend request

// TODO: 6/30/2019  avoid sending request to a producer thats already connected


}
