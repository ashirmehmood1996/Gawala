package com.android.example.gawala.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
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
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConsumerDashBoardActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView statusTectView, totalMilkTextView, totalAmountTextView, milkDemandTodayTextView;
    private ImageButton editDemadImageButton;
    private Button notAtHomeButton, gotoRequestsButton;
    private float mAmountOfMilk = 0;

    //firbase related
    private DatabaseReference rootRef;
    private String myId;
    private String producerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_dash_board);


        //check if user has connected to  a gawala or not ?
        //if yes then well and good other wise send to the request activity

        initFilds();
        attachListeners();
        pouplateReevantData();

    }


    private void initFilds() {
        totalMilkTextView = findViewById(R.id.tv_con_dash_total_milk);
        totalAmountTextView = findViewById(R.id.tv_con_dash_total_amount);
        milkDemandTodayTextView = findViewById(R.id.tv_con_dash_demand);
        editDemadImageButton = findViewById(R.id.ib_con_dash_edit_demand);
        notAtHomeButton = findViewById(R.id.bt_con_dash_not_at_home);


        statusTectView = findViewById(R.id.tv_con_dash_producer_status);
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
        statusTectView.setText("You first need to connect to a nearby producer to get banefited with this app");
        gotoRequestsButton.setVisibility(View.VISIBLE);
        editDemadImageButton.setEnabled(false);
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

    private void pouplateReevantData() {
    // TODO: 8/5/2019 first producer need to feed this data


        // TODO: 8/6/2019  remove later
        milkDemandTodayTextView.setText(mAmountOfMilk + " litre(0)");
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
                        milkDemandTodayTextView.setText(amountLitresTextView.getText().toString());
                        dialog.dismiss();
                    }
                })
                .show();


    }

    private void showNoMilkRecieveAlert(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Dont Want Milk at Home?? ")
                .setMessage("are you sure that you dont want the Milkseller to be at home today")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ConsumerDashBoardActivity.this, "logout successfull", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ConsumerDashBoardActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(this, "ops ! something went wrong, you were too quick", Toast.LENGTH_SHORT).show();
                }

        }
        return super.onOptionsItemSelected(item);
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