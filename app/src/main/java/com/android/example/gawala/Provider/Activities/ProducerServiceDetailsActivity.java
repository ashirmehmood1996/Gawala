package com.android.example.gawala.Provider.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
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

import com.android.example.gawala.Consumer.Activities.ProducerDetailActivty;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProducerServiceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private GoodModel goodModel;
    private TextView nameTextView, descTextView, priceTextView, typeTextView, unitTextView;
    private Button deleteItemButton;
    private ImageButton editNameImageButton, editDetailImageButton, editPriceImageButton, editTypeImageButton, ediotUnitImageButton;


    private ImageView serviceImageVew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producer_service_details);
        supportPostponeEnterTransition();//for trasition animation
        initFields();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle extras = getIntent().getExtras();
            String imageTransitionName = extras.getString(ProducerDetailActivty.EXTRA_ANIMAL_IMAGE_TRANSITION_NAME);
            serviceImageVew.setTransitionName(imageTransitionName);
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
        unitTextView = findViewById(R.id.tv_prod_goods_detail_unit);
        deleteItemButton = findViewById(R.id.bt_prod_goods_details_delete);
        serviceImageVew = findViewById(R.id.iv_prod_goods_detail_picture);

        editNameImageButton = findViewById(R.id.ib_prod_goods_detail_edit_name);
        editDetailImageButton = findViewById(R.id.ib_prod_goods_detail_edit_detail);
        editPriceImageButton = findViewById(R.id.ib_prod_goods_detail_edit_price);
        editTypeImageButton = findViewById(R.id.ib_prod_goods_detail_edit_type);
        ediotUnitImageButton = findViewById(R.id.ib_prod_goods_detail_edit_unit);

    }

    private void setData() {
        nameTextView.setText(goodModel.getName());
        descTextView.setText(goodModel.getDescription());
        priceTextView.setText(String.format("%s PKR", goodModel.getPrice()));
        typeTextView.setText(goodModel.getType());
        unitTextView.setText(goodModel.getUnit());

        if (goodModel.getImage_uri() != null && !goodModel.getImage_uri().isEmpty()) {
            Glide.with(this).load(goodModel.getImage_uri()).into(serviceImageVew);
        }
        supportStartPostponedEnterTransition();
    }

    private void attachListeners() {

        editDetailImageButton.setOnClickListener(this);
        editNameImageButton.setOnClickListener(this);
        editPriceImageButton.setOnClickListener(this);
        editTypeImageButton.setOnClickListener(this);
        ediotUnitImageButton.setOnClickListener(this);

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
                showEditFieldDialog(priceTextView, v, goodModel.getPrice());
                break;
            case R.id.ib_prod_goods_detail_edit_type:
                showTypeEditDialog(goodModel.getType());
                break;
            case R.id.ib_prod_goods_detail_edit_unit:
                showUnitsEditDialog(goodModel.getUnit());
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


        editFieldLinearLayout.findViewById(R.id.bt_dialog_prod_goods_detail_cancel).setOnClickListener(v12 -> dialog.cancel());
        editFieldLinearLayout.findViewById(R.id.bt_dialog_prod_goods_detail_confirm).setOnClickListener(v1 -> {
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
//                    case R.id.tv_prod_goods_detail_type:
//                        addToFirebase("type", newValue);
//                        goodModel.setType(newValue);
//
//                        break;
                case R.id.tv_prod_goods_detail_price:
                    addToFirebase("price", newValue);
                    goodModel.setPrice(newValue);

                    break;
            }

            dialog.cancel();

        });
        dialog.show();
        if (textView.getId() == R.id.tv_prod_goods_detail_price) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        }
        editText.setSelectAllOnFocus(true);
//        editText.req


    }

    private void addToFirebase(String fieldName, String newValue) {
        rootRef.child("goods").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(goodModel.getId())
                .child(fieldName)
                .setValue(newValue).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "data updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void showTypeEditDialog(String type) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Category");
// add a radio button list
        String[] typeArray = getResources().getStringArray(R.array.categories);

        final String[] newValue = {type};
        int checkedItem = 0; // cow
        for (int i = 0; i < typeArray.length; i++) {
            if (typeArray[i].equals(type)) {
                checkedItem = i;
                break;
            }
        }
        builder.setSingleChoiceItems(typeArray, checkedItem, (dialog, which) -> {
            // user checked an item
            newValue[0] = typeArray[which];
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            addToFirebase("type", newValue[0]);
            goodModel.setType(newValue[0]);
            typeTextView.setText(goodModel.getType());
            // user clicked OK
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUnitsEditDialog(String unit) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Unit");
// add a radio button list
        String[] unitArray = getResources().getStringArray(R.array.item_units);

        final String[] newValue = {unit};
        int checkedItem = 0; // cow
        for (int i = 0; i < unitArray.length; i++) {
            if (unitArray[i].equals(unit)) {
                checkedItem = i;
                break;
            }
        }
        builder.setSingleChoiceItems(unitArray, checkedItem, (dialog, which) -> {
            // user checked an item
            newValue[0] = unitArray[which];
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            addToFirebase("unit", newValue[0]);
            goodModel.setUnit(newValue[0]);
            unitTextView.setText(goodModel.getUnit());
            // user clicked OK
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteItem() {

        // TODO: 11/19/2019 LATER for production level for now we donot delte the item we need to think about it that this item can  be obtained by many consumers and cannot be simple deleted  may be can add a node like available =false and then after some time e,g, 30 days we will be able to delete the item
        Toast.makeText(this, "this item is obtained by many parties and cannot be delted until the contract is fullfilled, contact us for mode details", Toast.LENGTH_SHORT).show();
//        rootRef
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
