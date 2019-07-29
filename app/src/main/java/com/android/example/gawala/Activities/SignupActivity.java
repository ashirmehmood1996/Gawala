package com.android.example.gawala.Activities;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.example.gawala.NavProducerMapActivity;
import com.android.example.gawala.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private static final int RC_REGISTER = 101;
    private EditText numberEditText;
    private Button registerButton;
    private Spinner typeSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getSupportActionBar().setTitle("Sign up..");
        initFields();
        attachListeners();
    }


    private void initFields() {
        numberEditText = findViewById(R.id.et_register_number);
        registerButton = findViewById(R.id.bt_register_register);
        typeSpinner = findViewById(R.id.sp_register_type);
        //setSpinner
        setSpinner();
    }

    private void setSpinner() {
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.user_type, R.layout.support_simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeSpinnerAdapter);
    }

    private void attachListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform necessary checks
                String numebr = numberEditText.getText().toString();// TODO: 6/21/2019 check that of number is in valid format take help from tracker
                if (numebr.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "please fill out the fields first", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkAvailabilityOfNumber(numebr);

            }
        });
    }

    private void checkAvailabilityOfNumber(final String number) {
        FirebaseDatabase.getInstance().getReference().child("users")
                .orderByChild("number").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(SignupActivity.this, "this numeber is already registered", Toast.LENGTH_SHORT).show();

                } else {
                    createNewAccount();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createNewAccount() {

        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder()
                .setDefaultNumber(numberEditText.getText().toString()).build());
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), RC_REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_REGISTER) {
            if (resultCode == RESULT_OK) {
                updateDatabaseAndSendToReleventActivity();
            } else {
                Toast.makeText(this, "login unsuccessfull", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateDatabaseAndSendToReleventActivity() {
        HashMap<String, Object> userMap = new HashMap<>();

        final String type;
        if (typeSpinner.getSelectedItemPosition() == 0) {
            type = "producer";
        } else {
            type = "consumer";
        }
        userMap.put("number", numberEditText.getText().toString());
        userMap.put("type", type);

        FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    sendToRelevantActivity(type);
                } else {
                    Toast.makeText(SignupActivity.this, "unable to register please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToRelevantActivity(String userType) {

        if (userType != null && userType.equals("producer")) {
            //            showProgressBar(false);
            startActivity(new Intent(this, NavProducerMapActivity.class));
            finish();
        } else if (userType != null && userType.equals("consumer")) {
            //          showProgressBar(false);
            startActivity(new Intent(this, ConsumerActivity.class));
            finish();
        } else {
            Toast.makeText(this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
