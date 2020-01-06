package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Adapters.AcquiredGoodsAdapter;
import com.android.example.gawala.Consumer.fragments.AquiredGoodDetailFragment;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class AcquiredGoodsActivity extends AppCompatActivity implements AcquiredGoodsAdapter.Callback, AquiredGoodDetailFragment.Callback {

    private static final int RC_AQUIRED_GOOD_DETAILS = 1211;
    private static final String IS_CHANGED = "ischanged";
    private static final String TAG_DETAILS_FRAGMENT = "detailsFragment";
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

        getSupportActionBar().setTitle("My Demand");

        initFields();
        loadProducersListAndRespectiveServices();

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

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void loadProducersListAndRespectiveServices() {
        connectedProducersArrayList.clear();
        acquiredGoodArrayList.clear();
        mAlertDialog.show();
        acquiredGoodsAdapter.notifyDataSetChanged();


        rootRef.child("connected_producers")
                .child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (AcquiredGoodsActivity.this != null) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot providerSnap : dataSnapshot.getChildren()) {
                            String providerId = providerSnap.getKey();
//                            String providerName = providerSnap.child("name").getValue(String.class);
                            connectedProducersArrayList.add(providerId);
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
                        //do something if needed to e.g. show a banner that indicated that [please connect o a producer
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        // TO DO: 10/6/2019  change this query in order toa avoid whole data fetching  or we can change the database schema
//        //later deal with query
//        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists() && AcquiredGoodsActivity.this != null) {
//                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
//                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
//                                    if (clientSnap.getKey().equals(myId)) {
//
//                                        connectedProducersArrayList.add(producerSnap.getKey());
//
////                                        String name = producerSnap.child("name").getValue(String.class);
////                                        String number = producerSnap.child("number").getValue(String.class);
//                                    }
//                                }
//                            }
//                            if (connectedProducersArrayList.size() == 0) {
//                                emptyViewRelativeLayout.setVisibility(View.VISIBLE);
//                                mAlertDialog.dismiss();
//                            } else {
//                                fetchMyAquiredServices();
//                            }
//
//                        } else {
//                            emptyViewRelativeLayout.setVisibility(View.VISIBLE);
//                            Toast.makeText(AcquiredGoodsActivity.this, "no producers found", Toast.LENGTH_SHORT).show();
//                            mAlertDialog.dismiss();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Toast.makeText(AcquiredGoodsActivity.this, "fetching producers step was cancelled due to error:" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        AquiredGoodDetailFragment aquiredGoodDetailFragment = (AquiredGoodDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_DETAILS_FRAGMENT);
        if (aquiredGoodDetailFragment != null) {
            fragmentTransaction.remove(aquiredGoodDetailFragment);
        }
        aquiredGoodDetailFragment = AquiredGoodDetailFragment.newInstance(acquiredGoodArrayList.get(pos), pos);
        aquiredGoodDetailFragment.setCallback(this);
        aquiredGoodDetailFragment.show(fragmentTransaction, TAG_DETAILS_FRAGMENT);

    }

    @Override
    public void onItemUpdated(int position, AcquiredGoodModel acquiredGoodModel) {
        acquiredGoodArrayList.set(position, acquiredGoodModel);
        acquiredGoodsAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemDeleted(int position) {
        acquiredGoodArrayList.remove(position);
        acquiredGoodsAdapter.notifyItemRemoved(position);
        acquiredGoodsAdapter.notifyItemRangeChanged(position, acquiredGoodArrayList.size());
        if (acquiredGoodArrayList.isEmpty()) {
            emptyViewRelativeLayout.setVisibility(View.VISIBLE);
        }

    }
}