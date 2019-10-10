package com.android.example.gawala.Producer.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.Producer.Utils.ProducerFirebaseHelper;
import com.android.example.gawala.R;


public class ProducerAddServiceActivity extends AppCompatActivity {
    private EditText nameEditText, descriptionEditText, typeEditText, priceEditText;
    private Button addProductButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_add_service);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFields();
        attchListeners();
    }

    private void initFields() {
        nameEditText = findViewById(R.id.et_add_service_name);
        descriptionEditText = findViewById(R.id.et_add_service_desc);
        priceEditText = findViewById(R.id.et_add_service_price);
        typeEditText = findViewById(R.id.et_add_service_type);
        addProductButton=findViewById(R.id.bt_add_service);
    }

    private void attchListeners() {
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

    }

    private void showDialog() {
        String name =nameEditText.getText().toString();
        String desc =descriptionEditText.getText().toString();
        String price =priceEditText.getText().toString();
        String type =typeEditText.getText().toString();


        if (TextUtils.isEmpty(name)||TextUtils.isEmpty(desc)
                ||TextUtils.isEmpty(price)||TextUtils.isEmpty(type)){
            Toast.makeText(this, "plaese fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        final GoodModel goodModel=new GoodModel(name,desc,price,type);

        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure?")
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProducerFirebaseHelper.addNewGood(goodModel,ProducerAddServiceActivity.this);
                    }
                })
                .setNegativeButton("cancel", null).show();
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
