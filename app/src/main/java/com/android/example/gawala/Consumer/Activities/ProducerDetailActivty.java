package com.android.example.gawala.Consumer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Adapters.GoodsAdapter;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProducerDetailActivty extends AppCompatActivity implements GoodsAdapter.CallBack {

    private String producerID;

    private TextView nameTextView, numberTextView;

    private RecyclerView recyclerView;
    private ArrayList<GoodModel> goodModelArrayList;
    private GoodsAdapter goodsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_detail_activty);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initFields();
        loadThisProducerGoods();


    }

    private void initFields() {
        producerID = getIntent().getStringExtra("producer_id");
        nameTextView = findViewById(R.id.tv_prod_detail_name);
        nameTextView.setText(getIntent().getStringExtra("name"));
        numberTextView = findViewById(R.id.tv_prod_detail_number);
        numberTextView.setText(getIntent().getStringExtra("number"));

        recyclerView = findViewById(R.id.rv_prod_detail);
        goodModelArrayList = new ArrayList<>();
        goodsAdapter = new GoodsAdapter(goodModelArrayList, this);
        recyclerView.setAdapter(goodsAdapter);

    }

    private void loadThisProducerGoods() {
        FirebaseDatabase.getInstance().getReference()
                .child("goods").child(producerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
                        GoodModel goodModel = goodSnap.getValue(GoodModel.class);
                        goodModelArrayList.add(goodModel);
                        goodsAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ProducerDetailActivty.this, "this producer has listed no services at the time", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onGoodItemClick(int position) {
        GoodModel currentModel = goodModelArrayList.get(position);
        Intent intent = new Intent(this, ConProducerServiceDetailsActivty.class);
        intent.putExtra("goods_model", currentModel);
        intent.putExtra("producer_id",producerID);
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