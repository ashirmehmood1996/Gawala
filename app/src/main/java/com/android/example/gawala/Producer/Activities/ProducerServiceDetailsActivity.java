package com.android.example.gawala.Producer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ProducerServiceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private GoodModel goodModel;
    private TextView nameTextView, descTextView, priceTextView, typeTextView;
    private Button deleteItemButton;
    private ImageButton editNameImageButton, editDetailImageButton, editPriceImageButton, editTypeImageButton;


    private ImageView serviceImageVew;

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
        deleteItemButton = findViewById(R.id.bt_prod_goods_details_delete);
        serviceImageVew = findViewById(R.id.iv_prod_goods_detail_picture);

        editNameImageButton = findViewById(R.id.ib_prod_goods_detail_edit_name);
        editDetailImageButton = findViewById(R.id.ib_prod_goods_detail_edit_detail);
        editPriceImageButton = findViewById(R.id.ib_prod_goods_detail_edit_price);
        editTypeImageButton = findViewById(R.id.ib_prod_goods_detail_edit_type);

    }

    private void setData() {
        nameTextView.setText(goodModel.getName());
        descTextView.setText(goodModel.getDescription());
        priceTextView.setText(String.format("%s PKR", goodModel.getPrice()));
        typeTextView.setText(goodModel.getType());

        if (goodModel.getImage_uri() != null && !goodModel.getImage_uri().isEmpty()) {
            Glide.with(this).load(goodModel.getImage_uri()).into(serviceImageVew);
        }
    }

    private void attachListeners() {

        editDetailImageButton.setOnClickListener(this);
        editNameImageButton.setOnClickListener(this);
        editPriceImageButton.setOnClickListener(this);
        editTypeImageButton.setOnClickListener(this);


        deleteItemButton.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_prod_goods_detail_edit_name:
                showEditFieldDialog(nameTextView, v, goodModel.getName());
                break;
            case R.id.ib_prod_goods_detail_edit_detail:
                showEditFieldDialog(descTextView, v, goodModel.getDescription());
                break;
            case R.id.ib_prod_goods_detail_edit_price:
                String price = goodModel.getPrice();
                showEditFieldDialog(priceTextView, v, goodModel.getPrice());
                break;
            case R.id.ib_prod_goods_detail_edit_type:
                showEditFieldDialog(typeTextView, v, goodModel.getType());
                break;
            case R.id.bt_prod_goods_details_delete:
                deleteItem();
                break;
        }
    }


    private void showEditFieldDialog(TextView textView, View v, String text) {
        LinearLayout editFieldLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_field_layout, null);

        EditText editText = editFieldLinearLayout.findViewById(R.id.et_dialog_prod_goods_detail_edit_field);

        editText.setText(text);

//        TextView titleTextView=editFieldLinearLayout.findViewById(R.id.tv_dialog_prod_goods_detail_title);
//        titleTextView.setText();

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(editFieldLinearLayout).create();


        editFieldLinearLayout.findViewById(R.id.bt_dialog_prod_goods_detail_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
            }
        });
        editFieldLinearLayout.findViewById(R.id.bt_dialog_prod_goods_detail_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue = editText.getText().toString().trim();
                if (newValue.isEmpty()) {
                    Toast.makeText(ProducerServiceDetailsActivity.this, "field cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                textView.setText(newValue);
                switch (textView.getId()) {
                    case R.id.tv_prod_goods_detail_name:
                        addToFirebase("name", newValue);
                        goodModel.setName(newValue);
                        break;
                    case R.id.tv_prod_goods_detail_desc:
                        addToFirebase("description", newValue);
                        goodModel.setDescription(newValue);

                        break;
                    case R.id.tv_prod_goods_detail_type:
                        addToFirebase("type", newValue);
                        goodModel.setType(newValue);

                        break;
                    case R.id.tv_prod_goods_detail_price:
                        addToFirebase("price", newValue);
                        goodModel.setPrice(newValue);

                        break;
                }

                dialog.cancel();

            }
        });
        dialog.show();
        if (textView.getId() == R.id.tv_prod_goods_detail_price) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        }
        editText.setSelectAllOnFocus(true);
//        editText.req


    }

    private void addToFirebase(String fieldName, String newValue) {
        FirebaseDatabase.getInstance().getReference()
                .child("goods").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(goodModel.getId())
                .child(fieldName)
                .setValue(newValue).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "data updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteItem() {

        // TODO: 11/19/2019 for now we donot delte the item we need to think about it that this item can  be obtained by many consumers and cannot be simple deleted  may be can add a node like available =false and then after some time e,g, 30 days we will be able to delete the item
        Toast.makeText(this, "this item is obtained by many parties and cannot be delted until the contract is fullfilled, contact us for mode details", Toast.LENGTH_SHORT).show();
//        FirebaseDatabase.getInstance().getReference()
//                .child("goods").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(goodModel.getId()).child(goodModel.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()){
//                    Toast.makeText(ProducerServiceDetailsActivity.this, "delted successfully", Toast.LENGTH_SHORT).show();
//                    finish();
//                }else {
//
//                }
//            }
//        });
    }
}
