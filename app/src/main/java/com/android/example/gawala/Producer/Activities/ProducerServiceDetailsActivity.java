package com.android.example.gawala.Producer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;

public class ProducerServiceDetailsActivity extends AppCompatActivity {
    private GoodModel goodModel;
    private TextView nameTextView, descTextView, priceTextView, typeTextView;
    private Button deleteItemButton, editItemButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_service_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFields();
        setData();
        attachListeners();

    }

    private void initFields() {
        Intent intent = getIntent();
        goodModel = (GoodModel) intent.getSerializableExtra("goods_model");

        nameTextView = findViewById(R.id.tv_prod_goods_detail_name);
        descTextView = findViewById(R.id.tv_prod_goods_detail_desc);
        priceTextView = findViewById(R.id.tv_prod_goods_detail_price);
        typeTextView = findViewById(R.id.tv_prod_goods_detail_type);
        deleteItemButton = findViewById(R.id.bt_prod_goods_delete);
        editItemButton = findViewById(R.id.bt_prod_goods_edit);

    }

    private void setData() {
        nameTextView.setText(Html.fromHtml("<b>Name: </b>" + goodModel.getName()));
        descTextView.setText(Html.fromHtml("<b>Description: </b>" + goodModel.getDescription()));
        priceTextView.setText(Html.fromHtml("<b>Price: </b>" + goodModel.getPrice() + " PKR"));
        typeTextView.setText(Html.fromHtml("<b>Type: </b>" + goodModel.getType()));
    }

    private void attachListeners() {
        deleteItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProducerServiceDetailsActivity.this, "will be deal later", Toast.LENGTH_SHORT).show();
            }
        });
        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProducerServiceDetailsActivity.this, "will be deal later", Toast.LENGTH_SHORT).show();

            }
        });
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
