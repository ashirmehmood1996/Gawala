package com.android.example.gawala.Generel.Activities;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Activities.ConsumerMainActivity;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Provider.Activities.ProviderMainActivity;
import com.android.example.gawala.R;
import com.android.example.gawala.Transporter.Activities.TransporterRideActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class SignupActivity extends AppCompatActivity {

    private static final int RC_REGISTER = 101;
    private CountryCodePicker countryCodePicker;
    private EditText nameEditText, numberEditText;
    private Button registerButton;
    private Spinner typeSpinner;
    private String mNumber = "";
    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initFields();
        attachListeners();
    }


    private void initFields() {

        nameEditText = findViewById(R.id.et_register_name);
        countryCodePicker = findViewById(R.id.ccp_register);
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
        numberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    typeSpinner.performClick();
                    typeSpinner.requestFocus();
                }
                return false;
            }
        });


        registerButton.setOnClickListener(v -> {
            //perform necessary checks
            String numebr = numberEditText.getText().toString();

            String name = nameEditText.getText().toString();
            if (numebr.isEmpty() || name.isEmpty()) {
                Toast.makeText(getApplicationContext(), "please fill out the fields first", Toast.LENGTH_SHORT).show();
                return;
            }


            if (isInternetAvailable()) {
                initializeDialog();
                mAlertDialog.show();
                mNumber = "+" + countryCodePicker.getSelectedCountryCode() + numebr;
                checkAvailabilityOfNumber(mNumber);
            } else {
                Toast.makeText(getApplicationContext(), "please check your internet connection", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void checkAvailabilityOfNumber(final String number) {
        rootRef.child("users")
                .orderByChild("number").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (SignupActivity.this != null) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(getApplicationContext(), "this numeber is already registered", Toast.LENGTH_SHORT).show();
                        if (mAlertDialog.isShowing())
                            mAlertDialog.dismiss();

                    } else {
                        createNewAccount(number);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createNewAccount(String number) {

        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder()
                .setDefaultNumber(number).build());
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
                Toast.makeText(getApplicationContext(), "login unsuccessfull", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateDatabaseAndSendToReleventActivity() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameEditText.getText().toString()).build();
        currentUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (SignupActivity.this != null) {
                    if (mAlertDialog != null && mAlertDialog.isShowing())//null senario can only accur here because this code can run even when the acivity is dead
                        mAlertDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "profile updated failed", Toast.LENGTH_SHORT).show();
                    }
                }
                addClientToDatabase();

            } else {
                Toast.makeText(this, "problkem in updating profile ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addClientToDatabase() {
        HashMap<String, Object> userMap = new HashMap<>();

        final String type = (String) typeSpinner.getSelectedItem();
//        if (typeSpinner.getSelectedItemPosition() == 0) {
//            type = "producer";
//        } else {
//            type = "consumer";
//        }
        userMap.put("number", mNumber);
        userMap.put("name", nameEditText.getText().toString().trim());
        userMap.put("type", type);


        rootRef.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendToRelevantActivity(type);
            } else {
                Toast.makeText(SignupActivity.this, "unable to register please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToRelevantActivity(String userType) {

        if (userType != null && userType.equals(getResources().getString(R.string.provider))) {
            SharedPreferenceUtil.storeValue(getApplicationContext(), getResources().getString(R.string.type_key), (String) typeSpinner.getSelectedItem());
            startActivity(new Intent(this, ProviderMainActivity.class));
            finish();
        } else if (userType != null && userType.equals(getResources().getString(R.string.consumer))) {
            SharedPreferenceUtil.storeValue(getApplicationContext(), getResources().getString(R.string.type_key), (String) typeSpinner.getSelectedItem());
            startActivity(new Intent(this, ConsumerMainActivity.class));
            finish();
        } else if (userType != null && userType.equals(getResources().getString(R.string.transporter))) {
            SharedPreferenceUtil.storeValue(getApplicationContext(), getResources().getString(R.string.type_key), (String) typeSpinner.getSelectedItem());
            startActivity(new Intent(this, TransporterRideActivity.class));
            finish();
        } else {
            Toast.makeText(this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
//copy paste stuff to relevan places plus ad the user all basic info in the shared pre at the first login in order to avoin any in convinience