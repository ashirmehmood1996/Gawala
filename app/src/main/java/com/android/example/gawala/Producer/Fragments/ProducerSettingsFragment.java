package com.android.example.gawala.Producer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProducerSettingsFragment extends DialogFragment {
    //private static String USER_ID = "userIdKey";

    private Switch locationSwitch;

    private ImageButton backImageButton;

    public static ProducerSettingsFragment getInstance(/*String userId*/) {
//        Bundle bundle = new Bundle();
//        bundle.putString(USER_ID, userId);
        ProducerSettingsFragment producerSettingsFragment = new ProducerSettingsFragment();
//        producerSettingsFragment.setArguments(bundle);
        return producerSettingsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);

//        userId = getArguments().getString(USER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_producer_settings, container, false);
        initFields(rootView);
        attachListeners();
        fetchMyState();

        return rootView;

    }


    private void initFields(View rootView) {
        locationSwitch = rootView.findViewById(R.id.sw_frag_prod_settings_share_location);
        backImageButton = rootView.findViewById(R.id.ib_frag_prod_settings_back_button);
    }


    private void attachListeners() {
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> changeStateInFirebase(isChecked));
        backImageButton.setOnClickListener(v -> {
            dismiss();
        });

    }

    private void changeStateInFirebase(boolean shouldShare) {
        FirebaseDatabase.getInstance().getReference()
                .child("share_location")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(shouldShare);


    }

    private void fetchMyState() {
        locationSwitch.setEnabled(false);
        FirebaseDatabase.getInstance().getReference()
                .child("share_location")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (ProducerSettingsFragment.this != null) {
                            if (dataSnapshot.exists()) {
                                boolean isShared = dataSnapshot.getValue(Boolean.class);
                                locationSwitch.setChecked(isShared);

                            } else {
                                locationSwitch.setChecked(false);
                            }
                            locationSwitch.setEnabled(true);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
