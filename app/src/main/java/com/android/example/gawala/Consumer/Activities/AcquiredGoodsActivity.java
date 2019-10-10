package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Adapters.AcquiredGoodsAdapter;
import com.android.example.gawala.Consumer.Models.AquiredGoodModel;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AcquiredGoodsActivity extends AppCompatActivity implements AcquiredGoodsAdapter.Callback {

    private String myId;
    private DatabaseReference rootRef;
    private ArrayList<String> connectedProducersArrayList;

    private ArrayList<AquiredGoodModel> acquiredGoodArrayList;
    private RecyclerView acquiredGoodsRecyclerView;
    private AcquiredGoodsAdapter acquiredGoodsAdapter;
    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_acquired_goods);

        getSupportActionBar().setTitle("My Services");

        initFields();
    }

    private void initFields() {
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        connectedProducersArrayList = new ArrayList<>();

        acquiredGoodArrayList = new ArrayList<>();
        acquiredGoodsRecyclerView = findViewById(R.id.rv_con_acquired_goods);
        acquiredGoodsAdapter = new AcquiredGoodsAdapter(acquiredGoodArrayList, this);
        acquiredGoodsRecyclerView.setAdapter(acquiredGoodsAdapter);

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
        // TODO: 10/6/2019  change this qury in order toa avoid whole data fatching  or we can change the database schema
        //lter deal with query
        rootRef.child("clients")/*.orderByChild("number").equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot producerSnap : dataSnapshot.getChildren()) {
                                for (DataSnapshot clientSnap : producerSnap.getChildren()) {
                                    if (clientSnap.getKey().equals(myId)) {

                                        connectedProducersArrayList.add(producerSnap.getKey());
//                                        String name = producerSnap.child("name").getValue(String.class);
//                                        String number = producerSnap.child("number").getValue(String.class);
                                    }
                                }
                            }
                            if (connectedProducersArrayList.size()==0){
                                mAlertDialog.dismiss();
                            }

                            fetchMyAquiredServices();
                        } else {
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
        for (final String producerID : connectedProducersArrayList) {
            rootRef.child("demand")
                    .child(producerID).child(myId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
                                    fetchGoodDetailFromFireabse(goodSnap.getKey(),goodSnap.child("demand").getValue(String.class),producerID);

                                }
                                acquiredGoodsAdapter.notifyDataSetChanged();
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
        FirebaseDatabase.getInstance().getReference()
                .child("goods").child(producerID).child(goodID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            GoodModel goodModel=dataSnapshot.getValue(GoodModel.class);
                            acquiredGoodArrayList.add(new AquiredGoodModel(demand, producerID, goodModel));
                            acquiredGoodsAdapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onAcquiredGoodClicked(int pos) {
        Intent intent=new Intent(this,AquiredGoodDetailActivity.class);
        intent.putExtra("acquired_goods_model",acquiredGoodArrayList.get(pos));

        startActivity(intent);
    }
}