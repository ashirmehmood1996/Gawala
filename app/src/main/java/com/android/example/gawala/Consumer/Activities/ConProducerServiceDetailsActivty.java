package com.android.example.gawala.Consumer.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ConProducerServiceDetailsActivty extends AppCompatActivity {
    private GoodModel goodModel;

    private TextView nameTextView, descTextView, priceTextView, typeTextView;
    private Button addItemToDemandButton;
    private String producerID;
    private ImageView servicePictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_producer_service_details_activty);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // TODO: 10/8/2019 we need to check weather the user has already added this service in demand or not
        initFields();
        setData();
        attachListeners();

    }

    private void initFields() {
        Intent intent = getIntent();
        goodModel = (GoodModel) intent.getSerializableExtra("goods_model");
        producerID = intent.getStringExtra("producer_id");


        nameTextView = findViewById(R.id.tv_con_prod_goods_detail_name);
        descTextView = findViewById(R.id.tv_con_prod_goods_detail_desc);
        priceTextView = findViewById(R.id.tv_con_prod_goods_detail_price);
        typeTextView = findViewById(R.id.tv_con_prod_goods_detail_type);
        addItemToDemandButton = findViewById(R.id.bt_con_prod_goods_edit);
        servicePictureImageView = findViewById(R.id.iv_con_prod_goods_detail_picture);

    }

    private void setData() {
//        nameTextView.setText(Html.fromHtml("<b>Name: </b>" + goodModel.getName()));
//        descTextView.setText(Html.fromHtml("<b>Description: </b>" + goodModel.getDescription()));
//        priceTextView.setText(Html.fromHtml("<b>Price: </b>" + goodModel.getPrice() + " PKR"));
//        typeTextView.setText(Html.fromHtml("<b>Type: </b>" + goodModel.getType()));
        nameTextView.setText(goodModel.getName());
        descTextView.setText(goodModel.getDescription());
        priceTextView.setText(String.format("%s PKR", goodModel.getPrice()));
        typeTextView.setText(goodModel.getType());


        if (goodModel.getImage_uri() != null && !goodModel.getImage_uri().isEmpty()) {
            Glide.with(this).load(goodModel.getImage_uri()).into(servicePictureImageView);
        }
    }


    private void attachListeners() {

        addItemToDemandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlert();

            }
        });
    }

    private void showAlert() {
        new AlertDialog.Builder(this)
                .setTitle("plase confirm?")
                .setMessage("Adding this good will add additional amount to your monthly bills. tap confirm to add in demand.")
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addGoodToFireBase();

                    }
                })
                .setNegativeButton("cancel", null).show();

    }

    private void addGoodToFireBase() {

        // TODO: 10/8/2019 now we can addd good like that in demand  we will then add some restrictions like amunt of units needed and others if any as a sibling to id

        HashMap<String, Object> goodsMap = new HashMap<>();
        goodsMap.put("demand", "1"); //setting initial demand to 1 unit late rthe consumer will edit according to the need

        FirebaseDatabase.getInstance().getReference()
                .child("demand")
                .child(producerID)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//consumerID
                .child(goodModel.getId())//good id that was generated at the time this product was introduced by consumer
//                .child("consumer_id")//adding consumer id in order to be able to make queries
                .setValue(goodsMap).addOnCompleteListener(task -> {
            if (ConProducerServiceDetailsActivty.this != null) {
                Toast.makeText(getApplicationContext(), "Good added Successfully", Toast.LENGTH_SHORT).show();
                // TODO: 10/8/2019  we need to change some UI like changing the button or text of button to remove from demand
                // TODO: 11/22/2019   need to check that if demand is already added and then we send the user to demand detail activty rather than keeping the same activity

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
