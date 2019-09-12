package com.android.example.gawala.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.R;
import com.android.example.gawala.Utils.Firebase.ConsumerFirebaseHelper;
import com.android.example.gawala.Utils.UtilsMessaging;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;

public class ConsumerDashBoardActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView producerStatustextView, totalMilkTextView, totalAmountTextView, milkDemandTodayTextView;
    private ImageButton editDemadImageButton;
    private Button notAtHomeButton, gotoRequestsButton;
    private float mAmountOfMilk = 0;

    //firbase related
    private DatabaseReference rootRef;
    private String myId;
    private String producerId;
    private String producuerKey;
    private String producerStatus;
    private String milkPrice;
    private boolean mIsAtHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_dash_board);


        //check if user has connected to  a gawala or not ?
        //if yes then well and good other wise send to the request activity

        initFilds();
        attachListeners();
        pouplateRelevantData();
        UtilsMessaging.initFCM();

    }


    private void initFilds() {
        totalMilkTextView = findViewById(R.id.tv_con_dash_total_milk);
        totalAmountTextView = findViewById(R.id.tv_con_dash_total_amount);
        milkDemandTodayTextView = findViewById(R.id.tv_con_dash_demand);
        editDemadImageButton = findViewById(R.id.ib_con_dash_edit_demand);
        notAtHomeButton = findViewById(R.id.bt_con_dash_not_at_home);


        producerStatustextView = findViewById(R.id.tv_con_dash_producer_status);
        gotoRequestsButton = findViewById(R.id.bt_con_dash_goto_requests);

        //date related
        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void attachListeners() {
        editDemadImageButton.setOnClickListener(this);
        notAtHomeButton.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        checkIfUserIsCoonnectedToProducer();
        super.onStart();
    }

    private void checkIfUserIsCoonnectedToProducer() {
        //lter deal with query
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            boolean found = false;
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {
                                        producerId = dataSnapshot.getChildren().iterator().next().getKey();//producer key
                                        found = true;
                                        break;
                                    }

                                }

                            }

                            if (!found)
                                upDateUiForNoProducerConnection();
                            // TODO: 7/14/2019  later change the query along with the change in data
                        } else {

                            upDateUiForNoProducerConnection();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void upDateUiForNoProducerConnection() {
        producerStatustextView.setText("You first need to connect to a nearby producer to get banefited with this app");
        gotoRequestsButton.setVisibility(View.VISIBLE);
        editDemadImageButton.setEnabled(false); //// FIXME: 8/25/2019 see if they are enabled later or not
        notAtHomeButton.setEnabled(false);
        gotoRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRequestActivity();
            }
        });



    }

    private void sendUserToRequestActivity() {

        startActivity(new Intent(this, ConsumerRequestsActivity.class));
    }

    private void pouplateRelevantData() {

        // FIXME: 8/25/2019 later the consumer id be already fetchedand saved in shared preference

        //first fetch the producer id
        //// TODO: 8/25/2019 iterating over al data is very bad but will be taken care of in future by makkin queries or prefetching the producer id one time
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {

                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {
                                        producuerKey = producerSnap.getKey();//producer key

                                        fetchReleventDataUsingKey(producuerKey);

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

    private void fetchReleventDataUsingKey(final String producuerKey) {
// TODO: 8/25/2019  remove all lkisteneres at the time of logout and even at the time of on pause or pon stop
        // FIXME: 8/25/2019 for now un necessary data is being fetched later deal with this
        rootRef.child("data").child(producuerKey)
                .child("live_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // fixme we will chekc for this only one tie when the activity is lgged in for now leter we will do that only at the time of sign up

                    if (!dataSnapshot.child("clients_data").child(myId).child("milk_demand").exists()) {
                        ConsumerFirebaseHelper.updateMilkDemand("1.5", producuerKey);
                    } else {
                        mAmountOfMilk = Float.parseFloat(dataSnapshot.child("clients_data").child(myId).child("milk_demand").getValue(String.class));
                        milkDemandTodayTextView.setText(mAmountOfMilk + " litre(s)");
                    }
                    if (!dataSnapshot.child("clients_data").child(myId).child("at_home").exists()) {
                        mIsAtHome = true;
                        ConsumerFirebaseHelper.atHome(mIsAtHome, producuerKey);
                    } else {
                        mIsAtHome = dataSnapshot.child("clients_data").child(myId).child("at_home").getValue(Boolean.class);
                        if (mIsAtHome) notAtHomeButton.setText("I am not at Home");
                        else notAtHomeButton.setText("I am at Home");

                    }

                    //outer variables
                    producerStatus = dataSnapshot.child("status").getValue(String.class);
                    milkPrice = dataSnapshot.child("milk_price").getValue(String.class);
                    if (producerStatus == null) {
                        Toast.makeText(ConsumerDashBoardActivity.this, "Producer status was null ", Toast.LENGTH_SHORT).show();
                        producerStatus="unknown";
                        producerStatustextView.setText(producerStatus);
                        return;
                    }
                    if (producerStatus.equals(getResources().getString(R.string.status_producer_inactive))) {
                        producerStatustextView.setTextColor(Color.GRAY);
                    } else if (producerStatus.equals(getResources().getString(R.string.status_producer_onduty))) {
                        producerStatustextView.setTextColor(Color.GREEN);
                    }
                    producerStatustextView.setText(producerStatus);
                    producerStatustextView.setText(producerStatus);
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
            case R.id.ib_con_dash_edit_demand:
                openEditDialog(v);

                break;
            case R.id.bt_con_dash_not_at_home:
                showNoMilkRecieveAlert(v);
                break;
        }
    }


    private void openEditDialog(View v) {

        final LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_set_litres, null);
        final TextView amountLitresTextView = linearLayout.findViewById(R.id.tv_dialog_set_litres_litres);
        amountLitresTextView.setText(mAmountOfMilk + " litre(s) ");
        ImageButton addImageButton = linearLayout.findViewById(R.id.ib_dialog_set_litres_add);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmountOfMilk += .5;
                amountLitresTextView.setText(mAmountOfMilk + " litre(s) ");
            }
        });
        ImageButton removeImageButton = linearLayout.findViewById(R.id.ib_dialog_set_litres_remove);
        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAmountOfMilk > 0) {
                    mAmountOfMilk -= .5;
                    amountLitresTextView.setText(mAmountOfMilk + " litre(s) ");
                }
            }
        });


        new AlertDialog.Builder(this)
                .setView(linearLayout)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        milkDemandTodayTextView.setText(mAmountOfMilk + " litre(s) ");
                        ConsumerFirebaseHelper.updateMilkDemand(mAmountOfMilk + "", producuerKey);
                        dialog.dismiss();
                    }
                })
                .show();


    }

    private void showNoMilkRecieveAlert(View v) {

        String message=null;
        String title=null;
        if (mIsAtHome){
            title="Dont Want Milk at Home?? ";
            message="are you sure that you dont want the Milkseller to be at home today";

        }else {
            title="At home?";
            message="Want milk at home ?";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mIsAtHome){
                            ConsumerFirebaseHelper.atHome(false, producuerKey);
                            mIsAtHome=false;
                        }else {
                            ConsumerFirebaseHelper.atHome(true, producuerKey);
                            mIsAtHome=true;
                        }

                        // TODO: 8/6/2019 update a field in database that will let the producer know that today this consumers house is not in visit list
                    }
                }).setNegativeButton("cancel", null).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.consumer_dash_board, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_consumer_my_provider:
                startActivity(new Intent(this, ConsumerRequestsActivity.class));
                break;
            case R.id.nav_consumer_logout:

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    showLogoutDialogue();
                } else {
                    Toast.makeText(this, "ops ! something went wrong, you were too quick", Toast.LENGTH_SHORT).show();
                }

        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialogue() {
        //logout code
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("press logout to continue..")
                .setPositiveButton("logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessagingTokenAndLogout();
                    }
                }).setNegativeButton("cancel", null);
        builder.show();
    }

    private void deleteMessagingTokenAndLogout() {
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("messaging_token").setValue("null").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AuthUI.getInstance()
                        .signOut(ConsumerDashBoardActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ConsumerDashBoardActivity.this, "logout successfull", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ConsumerDashBoardActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(ConsumerDashBoardActivity.this, "logout failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConsumerDashBoardActivity.this, "logout failed please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
// TODO: 8/6/2019  put a braod cast receiver when GPs is turned on adn off and then trigger the location api
//// TODO: 8/5/2019 when consumer is attached to a producer  a sashboard will be displayed allowing to change the todays  demand of milk which(a constrant will be a specific hour or day)
// todo option to tell that I am not home.
//  history of number or liters milk delivered to the consumer with the amount alongside .


// .
// TODO: 8/5/2019   for future
//  .//show all available consumers to the approacher
//  //later we will show the . nearest on top and then others whcih will be done when the prodcers will share its repositiory location the
//  distance (not displacemnet ) will be calculated using the consumers own location no other location will be shared.
//  .

//complete the initialiation of data on both side where ever it is coming from and the update the dashboard