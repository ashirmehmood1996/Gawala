package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Activities.LoginActivity;
import com.android.example.gawala.R;
import com.android.example.gawala.Consumer.Utils.ConsumerFirebaseHelper;
import com.android.example.gawala.Generel.Utils.UtilsMessaging;
import com.android.example.gawala.Consumer.fragments.ClientSummeryFragment;
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

public class ConsumerDashBoardActivity extends AppCompatActivity implements View.OnClickListener {
    private Button notAtHomeButton, showSummeryButton, myProvidesButton, myServicesButton;

    //firbase related
    private DatabaseReference rootRef;
    private String myId;
    private String producerId;
    //    private String producuerKey;
    private String producerStatus;
    private String mCurrentMilkPrice;
    private boolean mIsAtHome;
    private String SUMMERY_FRAG_TAG = "SummerFragmentTag";
    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_dash_board);

        getSupportActionBar().setTitle("Consumer");

        initFilds();
        attachListeners();

        checkIfUserIsCoonnectedToProducer();
        mAlertDialog.show();
        UtilsMessaging.initFCM();

    }


    private void initFilds() {
        notAtHomeButton = findViewById(R.id.bt_con_dash_not_at_home);
        showSummeryButton = findViewById(R.id.bt_con_dash_show_summery);
        myProvidesButton = findViewById(R.id.bt_con_dash_my_providers);
        myServicesButton = findViewById(R.id.bt_con_dash_my_services);

        //date related
        rootRef = FirebaseDatabase.getInstance().getReference();
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initializeDialog();
    }

    private void attachListeners() {
        notAtHomeButton.setOnClickListener(this);
        showSummeryButton.setOnClickListener(this);
        myProvidesButton.setOnClickListener(this);
        myServicesButton.setOnClickListener(this);
    }


    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
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
                                        fetchReleventDataUsingKey(producerId);
                                        break;
                                    }

                                }

                            }

                            if (!found) {
                                mAlertDialog.dismiss();
                                upDateUiForNoProducerConnection();
                            }
                            // TODO: 7/14/2019  later change the query along with the change in data
                        } else {
                            mAlertDialog.dismiss();
                            upDateUiForNoProducerConnection();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void upDateUiForNoProducerConnection() {
        notAtHomeButton.setEnabled(false);
    }

//    private void sendUserToRequestActivity() {
//
//        startActivity(new Intent(this, ConsumerRequestsActivity.class));
//    }
//
////    private void pouplateRelevantData() {
////
////        // FIXME: 8/25/2019 later the consumer id be already fetchedand saved in shared preference
////
////        //first fetch the producer id
////        //// TODO: 8/25/2019 iterating over al data is very bad but will be taken care of in future by makkin queries or prefetching the producer id one time
////        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
////                .addListenerForSingleValueEvent(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                        if (dataSnapshot.exists()) {
////
////                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
////
////                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
////                                    if (clientSnap.getKey().equals(myId)) {
////                                        producuerKey = producerSnap.getKey();//producer key
////
////                                        fetchReleventDataUsingKey(producuerKey);
////
////                                        break;
////                                    }
////                                }
////                            }
////
////                            // TODO: 7/14/2019  later change the query along with the change in data
////                        }
////
////                    }
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////
////                    }
////                });
////
////
////    }

    private void fetchReleventDataUsingKey(final String producuerKey) {
// TODO: 8/25/2019  remove all lkisteneres at the time of logout and even at the time of on pause or pon stop

        //used for setting and getting clients own data fields
        rootRef.child("clients")
                .child(producuerKey)
                .child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (!dataSnapshot.child("live_data").child("at_home").exists()) {
                                mIsAtHome = true;
                                ConsumerFirebaseHelper.atHome(mIsAtHome, producuerKey);
                            } else {
                                mIsAtHome = dataSnapshot.child("live_data").child("at_home").getValue(Boolean.class);
                                if (mIsAtHome) notAtHomeButton.setText("I am not at Home");
                                else notAtHomeButton.setText("I am at Home");

                            }

                        } else {
                            Toast.makeText(ConsumerDashBoardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        mAlertDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mAlertDialog.dismiss();

                    }
                });


//        //to get the producer related infromation
//        rootRef.child("data").child(producuerKey)
//                .child("live_data").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    //outer variables
//                    producerStatus = dataSnapshot.child("status").getValue(String.class);
//                    mCurrentMilkPrice = dataSnapshot.child("milk_price").getValue(String.class);
//                    if (producerStatus == null) {
//                        Toast.makeText(ConsumerDashBoardActivity.this, "Producer status was null ", Toast.LENGTH_SHORT).show();
//                        producerStatus = "unknown";
//                        producerStatustextView.setText(producerStatus);
//                        return;
//                    }
//                    if (producerStatus.equals(getResources().getString(R.string.status_producer_inactive))) {
//                        producerStatustextView.setTextColor(Color.GRAY);
//                    } else if (producerStatus.equals(getResources().getString(R.string.status_producer_onduty))) {
//                        producerStatustextView.setTextColor(Color.GREEN);
//                    }
//                    producerStatustextView.setText(producerStatus);
//                    producerStatustextView.setText(producerStatus);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//
//
//        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_con_dash_not_at_home:
                showNoMilkRecieveAlert(v);
                break;
            case R.id.bt_con_dash_show_summery:
                ClientSummeryFragment clientSummeryFragment = ClientSummeryFragment.newInstance(producerId);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.consumer_dash_board_fragment_container, clientSummeryFragment, SUMMERY_FRAG_TAG)
                        .commit();
                break;
            case R.id.bt_con_dash_my_providers:
                startActivity(new Intent(this, ConsumerRequestsActivity.class));
                break;
            case R.id.bt_con_dash_my_services:
                startActivity(new Intent(this, AcquiredGoodsActivity.class));
                break;
        }
    }


    private void showNoMilkRecieveAlert(View v) {

        String message = null;
        String title = null;
        if (mIsAtHome) {
            title = "Dont Want Milk at Home?? ";
            message = "are you sure that you dont want the Milkseller to be at home today";

        } else {
            title = "At home?";
            message = "Want milk at home ?";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mIsAtHome) {
                            ConsumerFirebaseHelper.atHome(false, producerId);
                            mIsAtHome = false;
                        } else {
                            ConsumerFirebaseHelper.atHome(true, producerId);
                            mIsAtHome = true;
                        }
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
            case R.id.nav_consumer_logout:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    showLogoutDialogue();
                } else {
                    Toast.makeText(this, "ops ! something went wrong, you were too quick", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
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

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag(SUMMERY_FRAG_TAG) != null) {
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(SUMMERY_FRAG_TAG)).commit();
        } else {
            super.onBackPressed();
        }

    }
}
// TODO: 8/6/2019  put a braod cast receiver when GPs is turned on adn off and then trigger the location api
