package com.android.example.gawala.Producer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.android.example.gawala.Generel.Adapters.GoodsAdapter;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProducerServicesActivty extends AppCompatActivity implements GoodsAdapter.CallBack {
    private FloatingActionButton addServiceButton;
    private DatabaseReference myGoodsRef;
    private ChildEventListener goodsListListener;

    private ArrayList<GoodModel> goodModelArrayList;
    private RecyclerView recyclerView;
    private GoodsAdapter goodsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prodcuer_services_activty);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFields();
        atachListeners();
    }

    private void initFields() {
        addServiceButton = findViewById(R.id.fab_prod_service_add_new);
        recyclerView = findViewById(R.id.rv_prod_service);
        goodModelArrayList = new ArrayList<>();
        goodsAdapter = new GoodsAdapter(goodModelArrayList, this);
        recyclerView.setAdapter(goodsAdapter);

        myGoodsRef = FirebaseDatabase.getInstance().getReference()
                .child("goods").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        initGoodsListener();
    }

    private void initGoodsListener() {
        goodsListListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
                goodModelArrayList.add(goodModel);
                goodsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GoodModel newGoodModel = dataSnapshot.getValue(GoodModel.class);
                for (int i = 0; i < goodModelArrayList.size(); i++) {
                    GoodModel goodModel = goodModelArrayList.get(i);
                    if (goodModel.getId().equals(newGoodModel.getId())) {
                        goodModelArrayList.set(i, newGoodModel); //// TODO: 10/5/2019  check wether this logic is good enough
                        break;
                    }
                }
                goodsAdapter.notifyDataSetChanged();


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void atachListeners() {
        addServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProducerServicesActivty.this, ProducerAddServiceActivity.class));
            }
        });


    }

    @Override
    protected void onStart() {
        myGoodsRef.addChildEventListener(goodsListListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        myGoodsRef.removeEventListener(goodsListListener);
        goodModelArrayList.clear();
        super.onStop();
    }

    @Override
    public void onGoodItemClick(int position) {
        GoodModel currentModel = goodModelArrayList.get(position);
        Intent intent = new Intent(this, ProducerServiceDetailsActivity.class);
        intent.putExtra("goods_model", currentModel);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }else {
            return super.onOptionsItemSelected(item);

        }
    }
}
