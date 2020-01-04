package com.android.example.gawala.Provider.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Activities.ProfileActivity;
import com.android.example.gawala.Provider.Adapters.TransportersAdapter;
import com.android.example.gawala.Provider.Models.TransportersModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProviderTransportersActivity extends AppCompatActivity implements TransportersAdapter.Callbacks {

    public static final String IS_FOR_SELECTION = "isForSelection";
    //data related
    private boolean isForSelection;
    private String myId;
    private DatabaseReference transportersRef;
    private ChildEventListener transportersChildEventListener;
    private ArrayList<TransportersModel> transportersModelArrayList;

    //Ui related
    private FloatingActionButton addNewTransporterButton;
    private RecyclerView transportersRecyclerView;
    private TransportersAdapter transportersAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_transporters);
        initFields();
        attachListeners();
        transportersRef.addChildEventListener(transportersChildEventListener);
    }

    @Override
    protected void onDestroy() {
        transportersRef.removeEventListener(transportersChildEventListener);
        super.onDestroy();
    }

    private void initFields() {
        isForSelection = getIntent().getBooleanExtra(IS_FOR_SELECTION, false);
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        transportersRef = rootRef.child(getResources().getString(R.string.transporter)).child(myId);
        transportersModelArrayList = new ArrayList<>();
        transportersAdapter = new TransportersAdapter(transportersModelArrayList, this, isForSelection);

        addNewTransporterButton = findViewById(R.id.fab_provider_transporters_add_new);
        transportersRecyclerView = findViewById(R.id.rv_provider_transporters);
        transportersRecyclerView.setAdapter(transportersAdapter);

        transportersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // TODO: 12/11/2019  laod transporter here
                String transporterId = dataSnapshot.getKey();
                rootRef.child("users").child(transporterId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    String number = dataSnapshot.child("number").getValue(String.class);
                                    String image_uri = dataSnapshot.child("profile_image_uri").getValue(String.class);
                                    transportersModelArrayList.add(new TransportersModel(transporterId, name, number, image_uri));
                                    transportersAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
        if (isForSelection) {
            addNewTransporterButton.hide();
        }

    }


    private void attachListeners() {
        addNewTransporterButton.setOnClickListener(v -> {
            showAddTransporterdialog();
        });
        // TODO: 12/11/2019   a dialog that will seach and display the transporter to be added and then add the transporter
    }

    private void showAddTransporterdialog() {

        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_add_transporter, null);

        LinearLayout searchContainer = linearLayout.findViewById(R.id.ll_dialog_add_transporter_search_container);
        TextView titleTextView = linearLayout.findViewById(R.id.tv_dialog_add_transporter_title);

        TextInputEditText editText = linearLayout.findViewById(R.id.et_dialog_add_transporter_edit_field);
        ImageButton searchImageButton = linearLayout.findViewById(R.id.ib_dialog_add_transporter_search);

        ProgressBar progressBar = linearLayout.findViewById(R.id.pb_dialog_add_transporter);


        LinearLayout transporterContainer = linearLayout.findViewById(R.id.ll_dialog_add_transporter_transporter_container);
        TextView nameTextView = linearLayout.findViewById(R.id.tv_dialog_add_transporter_name);
        TextView numberTextView = linearLayout.findViewById(R.id.tv_dialog_add_transporter_number);
        CircularImageView circularImageView = linearLayout.findViewById(R.id.civ_dialog_add_transporter_image);
        Button addButton = linearLayout.findViewById(R.id.bt_dialog_add_transporter_add);

        Dialog dialog = new AlertDialog.Builder(this).setView(linearLayout).create();


        searchImageButton.setOnClickListener(v -> {
            String key = editText.getText().toString().trim();

            if (key.isEmpty()) {
                Toast.makeText(this, "please enter the code from your transporter", Toast.LENGTH_SHORT).show();
                return;
            }
            rootRef.child("users").child(key)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                progressBar.setVisibility(View.GONE);

                                transporterContainer.setVisibility(View.VISIBLE);
                                titleTextView.setText("add Transporter");
                                String name = dataSnapshot.child("name").getValue(String.class);
                                String number = dataSnapshot.child("number").getValue(String.class);
                                String imageUri = dataSnapshot.child("profile_image_uri").getValue(String.class);

                                nameTextView.setText(name);
                                numberTextView.setText(number);
                                if (imageUri != null && !imageUri.isEmpty()) {
                                    Glide.with(ProviderTransportersActivity.this).load(imageUri).into(circularImageView);
                                }
                            } else {
                                titleTextView.setText("Add transporter");
                                progressBar.setVisibility(View.GONE);
                                searchContainer.setVisibility(View.VISIBLE);
                                editText.setError("no Transporter was associated with the provided ID, please make sure the ID is correct");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            searchContainer.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            titleTextView.setText("searching...");
            // TODO: 12/11/2019  fetch transporter
        });


        addButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            transporterContainer.setVisibility(View.GONE);
            titleTextView.setText("adding please wait...");
            addTransporterToFirebase(editText.getText().toString().trim(), dialog);
        });
        dialog.show();

    }

    private void addTransporterToFirebase(String key, Dialog dialog) {
        rootRef.child(getResources().getString(R.string.transporter))
                .child(myId)//provider id
                .child(key) //transporter id
                .setValue(key).addOnCompleteListener(task -> {
            Toast.makeText(this, "transporter added successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    @Override
    public void onTransporterClick(int position) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID, transportersModelArrayList.get(position).getId());
        intent.putExtra(ProfileActivity.REQUEST_USER_TYPE, getResources().getString(R.string.provider));
        intent.putExtra(ProfileActivity.PROVIDER_ID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        intent.putExtra(ProfileActivity.OTHER_USER, true);
        startActivity(intent);
    }

    @Override
    public void onAssignButtonClicked(int positon) {
        Intent intent = new Intent();
        intent.putExtra(getResources().getString(R.string.transporter_id_key), transportersModelArrayList.get(positon).getId());
        intent.putExtra(getResources().getString(R.string.transporter_name_key), transportersModelArrayList.get(positon).getName());
        intent.putExtra(getResources().getString(R.string.transporter_number_key), transportersModelArrayList.get(positon).getNumber());
        setResult(RESULT_OK, intent);
        finish();
    }
}
