package com.android.example.gawala.Consumer.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class AquiredGoodDetailFragment extends DialogFragment {
    private static final String ARG_AQUIRED_GOOD_MODEL = "acquired_goods_model";
    private static final String ARG_POSITION = "pos";
    private AcquiredGoodModel acquiredGoodModel;

    private TextView nameTextView, descTextView, priceTextView, typeTextView, demandTextView;
    private Button removeItemFromDemandButton;
    private ImageButton editDemandImageButton, backButton;
    private ImageView goodPictureImageView;

    private int mDemand = 0;

    private Callback callback;
    private int position;

    public static AquiredGoodDetailFragment newInstance(AcquiredGoodModel acquiredGoodModel, int position) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_AQUIRED_GOOD_MODEL, acquiredGoodModel);
        args.putInt(ARG_POSITION, position);
        AquiredGoodDetailFragment fragment = new AquiredGoodDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);

        if (getArguments() != null) {
            acquiredGoodModel = (AcquiredGoodModel) getArguments().getSerializable(ARG_AQUIRED_GOOD_MODEL);
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_aquired_good_detail, container, false);
        initFields(rootView);
        setData();
        attachListeners();
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home || item.getItemId() == R.id.ib_con_acquired_goods_detail_back) {
            dismiss();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initFields(View rootView) {
        nameTextView = rootView.findViewById(R.id.tv_con_acquired_goods_detail_name);
        descTextView = rootView.findViewById(R.id.tv_con_acquired_goods_detail_desc);
        priceTextView = rootView.findViewById(R.id.tv_con_acquired_goods_detail_price);
        typeTextView = rootView.findViewById(R.id.tv_con_acquired_goods_detail_type);


        demandTextView = rootView.findViewById(R.id.tv_acquired_goods_demand);
        editDemandImageButton = rootView.findViewById(R.id.ib_acquired_goods_edit_demand);
        backButton = rootView.findViewById(R.id.ib_con_acquired_goods_detail_back);
        goodPictureImageView = rootView.findViewById(R.id.iv_con_acquired_goods_detail_picture);

        removeItemFromDemandButton = rootView.findViewById(R.id.bt_con_acquired_goods_remove);

    }

    private void setData() {
        nameTextView.setText(acquiredGoodModel.getGoodModel().getName());
        typeTextView.setText(acquiredGoodModel.getGoodModel().getType());
        descTextView.setText(acquiredGoodModel.getGoodModel().getDescription());
        priceTextView.setText(String.format("%s PKR", acquiredGoodModel.getGoodModel().getPrice()));
        mDemand = Integer.parseInt(acquiredGoodModel.getDemand());
        demandTextView.setText(mDemand + " " + acquiredGoodModel.getGoodModel().getUnit());

        if (acquiredGoodModel.getGoodModel().getImage_uri() != null
                && !acquiredGoodModel.getGoodModel().getImage_uri().isEmpty()) {
            Glide.with(this).load(acquiredGoodModel.getGoodModel().getImage_uri())
                    .into(goodPictureImageView);
        }
    }

    private void attachListeners() {
        removeItemFromDemandButton.setOnClickListener(v -> showRemoveWarning());

        editDemandImageButton.setOnClickListener(v -> openEditDialog());
        backButton.setOnClickListener(v -> dismiss());
    }

    private void showRemoveWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Remove Item Permanently?")
                .setMessage("Are you sure that you want to remove this item from daily demand?")
                .setPositiveButton("Remove", (dialog, which) -> removeDemandItemFromFirebase()).setNegativeButton("cancel", null).show();

    }

    private void removeDemandItemFromFirebase() {
        rootRef.child("demand").child(acquiredGoodModel.getProducerId())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(acquiredGoodModel.getGoodModel().getId())
                .removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Item removed successfully", Toast.LENGTH_SHORT).show();
                callback.onItemDeleted(position);
                dismiss();
            } else {
                Toast.makeText(getActivity(),
                        "cannot proceed due to Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void openEditDialog() {

        final LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_units, null);
        final TextView amountLitresTextView = linearLayout.findViewById(R.id.tv_dialog_edit_unit);
        amountLitresTextView.setText(mDemand + " " + acquiredGoodModel.getGoodModel().getUnit());
        ImageButton addImageButton = linearLayout.findViewById(R.id.ib_dialog_edit_units_add);
        addImageButton.setOnClickListener(v -> {
            mDemand += 1;
            amountLitresTextView.setText(mDemand + " " + acquiredGoodModel.getGoodModel().getUnit());
        });
        ImageButton removeImageButton = linearLayout.findViewById(R.id.ib_dialog_edit_units_remove);
        removeImageButton.setOnClickListener(v -> {
            if (mDemand > 0) {
                mDemand -= 1;
                amountLitresTextView.setText(mDemand + " " + acquiredGoodModel.getGoodModel().getUnit());
            }
        });


        new AlertDialog.Builder(getActivity())
                .setView(linearLayout)
                .setPositiveButton("ok", (dialog, which) -> {
                    demandTextView.setText(mDemand + " " + acquiredGoodModel.getGoodModel().getUnit());
                    ConsumerFirebaseHelper.updateDemand(mDemand + "", acquiredGoodModel);
//                        ConsumerFirebaseHelper.updateMilkDemand(mDemand + "", producerId);
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "Demand Updated", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public interface Callback {
        void onItemUpdated(int position, AcquiredGoodModel acquiredGoodModel);

        void onItemDeleted(int position);
    }

}
