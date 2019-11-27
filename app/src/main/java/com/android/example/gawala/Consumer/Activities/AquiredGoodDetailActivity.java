package com.android.example.gawala.Consumer.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Consumer.Utils.ConsumerFirebaseHelper;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AquiredGoodDetailActivity extends AppCompatActivity {
    private AcquiredGoodModel acquiredGoodModel;

    private TextView nameTextView, descTextView, priceTextView, typeTextView, demandTextView;
    private Button removeItemFromDemandButton;
    private ImageButton editDemandImageButton;
    private ImageView goodPictureImageView;

    private int mDemand = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aquired_good_detail);

        getSupportActionBar().setTitle("Service Details");

        initFields();
        setData();
        attachListeners();


    }

    private void initFields() {
        Intent intent = getIntent();
        acquiredGoodModel = (AcquiredGoodModel) intent.getSerializableExtra("acquired_goods_model");

        nameTextView = findViewById(R.id.tv_con_acquired_goods_detail_name);
        descTextView = findViewById(R.id.tv_con_acquired_goods_detail_desc);
        priceTextView = findViewById(R.id.tv_con_acquired_goods_detail_price);
        typeTextView = findViewById(R.id.tv_con_acquired_goods_detail_type);

        demandTextView = findViewById(R.id.tv_acquired_goods_demand);
        editDemandImageButton = findViewById(R.id.ib_acquired_goods_edit_demand);
        goodPictureImageView = findViewById(R.id.iv_con_acquired_goods_detail_picture);

        removeItemFromDemandButton = findViewById(R.id.bt_con_acquired_goods_remove);

    }

    private void setData() {
        nameTextView.setText(acquiredGoodModel.getGoodModel().getName());
        typeTextView.setText(acquiredGoodModel.getGoodModel().getType());
        descTextView.setText(acquiredGoodModel.getGoodModel().getDescription());
        priceTextView.setText(String.format("%s PKR", acquiredGoodModel.getGoodModel().getPrice()));
        mDemand = Integer.parseInt(acquiredGoodModel.getDemand());
        demandTextView.setText(String.format("%d Unit(s)", mDemand));

        if (acquiredGoodModel.getGoodModel().getImage_uri() != null
                && !acquiredGoodModel.getGoodModel().getImage_uri().isEmpty()) {
            Glide.with(this).load(acquiredGoodModel.getGoodModel().getImage_uri())
                    .into(goodPictureImageView);
        }
    }

    private void attachListeners() {
        removeItemFromDemandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showRemoveWarning();


            }
        });

        editDemandImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialog();
            }
        });
    }

    private void showRemoveWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item Permanently?")
                .setMessage("Are you sure that you want to remove this item from daily demand?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeDemandItemFromFirebase();
                    }
                }).setNegativeButton("cancel", null).show();

    }

    private void removeDemandItemFromFirebase() {
        FirebaseDatabase.getInstance().getReference()
                .child("demand").child(acquiredGoodModel.getProducerId())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(acquiredGoodModel.getGoodModel().getId())
                .removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Item removed successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(),
                        "cannot proceed due to Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void openEditDialog() {

        final LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_set_litres, null);
        final TextView amountLitresTextView = linearLayout.findViewById(R.id.tv_dialog_set_litres_litres);
        amountLitresTextView.setText(mDemand + " Unit(s) ");
        ImageButton addImageButton = linearLayout.findViewById(R.id.ib_dialog_set_litres_add);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDemand += 1;
                amountLitresTextView.setText(mDemand + " Unit(s) ");
            }
        });
        ImageButton removeImageButton = linearLayout.findViewById(R.id.ib_dialog_set_litres_remove);
        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDemand > 0) {
                    mDemand -= 1;
                    amountLitresTextView.setText(mDemand + " Unit(s) ");
                }
            }
        });


        new AlertDialog.Builder(this)
                .setView(linearLayout)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        demandTextView.setText(mDemand + " Unit(s) ");
                        ConsumerFirebaseHelper.updateDemand(mDemand + "", acquiredGoodModel);
//                        ConsumerFirebaseHelper.updateMilkDemand(mDemand + "", producerId);
                        dialog.dismiss();
                        Toast.makeText(AquiredGoodDetailActivity.this, "Demand Updated", Toast.LENGTH_SHORT).show();//// TODO: 10/10/2019  this is a best case consideration later do some callback stuff because we dont know weather the data is upadted or not
                    }
                })
                .show();


    }


}
