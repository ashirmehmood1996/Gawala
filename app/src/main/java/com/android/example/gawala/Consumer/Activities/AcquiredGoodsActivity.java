package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Adapters.AcquiredGoodsAdapter;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class AcquiredGoodsActivity extends AppCompatActivity implements AcquiredGoodsAdapter.Callback {

    private String myId;
    private ArrayList<String> connectedProducersArrayList;

    private ArrayList<AcquiredGoodModel> acquiredGoodArrayList;
    private RecyclerView acquiredGoodsRecyclerView;
    private AcquiredGoodsAdapter acquiredGoodsAdapter;
    private AlertDialog mAlertDialog;

    private RelativeLayout emptyViewRelativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_acquired_goods);

        getSupportActionBar().setTitle("My Services");

        initFields();
    }

    private void initFields() {
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        connectedProducersArrayList = new ArrayList<>();

        acquiredGoodArrayList = new ArrayList<>();
        acquiredGoodsRecyclerView = findViewById(R.id.rv_con_acquired_goods);
        acquiredGoodsAdapter = new AcquiredGoodsAdapter(acquiredGoodArrayList, this);
        acquiredGoodsRecyclerView.setAdapter(acquiredGoodsAdapter);
        emptyViewRelativeLayout = findViewById(R.id.rl_con_acquired_goods_empty_view_container);

        initializeDialog();


    }

    @Override
    protected void onStart() {
        loadProducersListAndRespectiveServices();//doing it here for data updation
        super.onStart();
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void loadProducersListAndRespectiveServices() {
        connectedProducersArrayList.clear();
        acquiredGoodArrayList.clear();
        mAlertDialog.show();
        acquiredGoodsAdapter.notifyDataSetChanged();
        // TODO: 10/6/2019  change this query in order toa avoid whole data fetching  or we can change the database schema
        //later deal with query
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && AcquiredGoodsActivity.this != null) {
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {

                                        connectedProducersArrayList.add(producerSnap.getKey());

//                                        String name = producerSnap.child("name").getValue(String.class);
//                                        String number = producerSnap.child("number").getValue(String.class);
                                    }
                                }
                            }
                            if (connectedProducersArrayList.size() == 0) {
                                emptyViewRelativeLayout.setVisibility(View.VISIBLE);
                                mAlertDialog.dismiss();
                            } else {
                                fetchMyAquiredServices();
                            }

                        } else {
                            emptyViewRelativeLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(AcquiredGoodsActivity.this, "no producers found", Toast.LENGTH_SHORT).show();
                            mAlertDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AcquiredGoodsActivity.this, "fetching producers step was cancelled due to error:" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void fetchMyAquiredServices() {
        final boolean[] hasDemand = {false};
        for (final String producerID : connectedProducersArrayList) {
            rootRef.child("demand")
                    .child(producerID).child(myId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && AcquiredGoodsActivity.this != null) {
                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
                                    fetchGoodDetailFromFireabse(goodSnap.getKey(), goodSnap.child("demand").getValue(String.class), producerID);
                                }
                                hasDemand[0] = true;
                                if (emptyViewRelativeLayout.getVisibility() == View.VISIBLE) {
                                    emptyViewRelativeLayout.setVisibility(View.GONE);
                                }
                            } else {
                                if (!hasDemand[0]) {
                                    emptyViewRelativeLayout.setVisibility(View.VISIBLE);
                                } else {
                                    emptyViewRelativeLayout.setVisibility(View.GONE);
                                }
                            }
                            mAlertDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(AcquiredGoodsActivity.this, String.format("fetching Services step for %s was cancelled due to error:%s", producerID, databaseError.getMessage()), Toast.LENGTH_SHORT).show();
                            mAlertDialog.dismiss();

                        }
                    });
        }
    }

    private void fetchGoodDetailFromFireabse(String goodID, final String demand, final String producerID) {
        rootRef.child("goods").child(producerID).child(goodID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && AcquiredGoodsActivity.this != null) {
                            GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
                            acquiredGoodArrayList.add(new AcquiredGoodModel(demand, producerID, goodModel));
                            acquiredGoodsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplication(), "couldn't find this good", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onAcquiredGoodClicked(int pos) {
        Intent intent = new Intent(this, AquiredGoodDetailActivity.class);
        intent.putExtra("acquired_goods_model", acquiredGoodArrayList.get(pos));
        startActivity(intent);
    }
}