package com.android.example.gawala.Generel.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Consumer.Activities.ConsumerMainActivity;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Provider.Activities.ProviderMainActivity;

import com.android.example.gawala.R;
import com.android.example.gawala.Transporter.Activities.TransporterMainActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.util.Arrays;
import java.util.List;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_LOGIN = 1001;

    private CountryCodePicker countryCodePicker;
    private EditText numberEditText;
    private TextView wrongInputAlertTextView;
    private Button loginButton, signupButton;


    private String mPhoneNumber = "";
    private AlertDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        getSupportActionBar().setTitle("Login..");
        initFields();
        attachListeners();
    }

    private void initFields() {
        countryCodePicker = findViewById(R.id.ccp_login);
        numberEditText = findViewById(R.id.et_login_number);
        wrongInputAlertTextView = findViewById(R.id.tv_login_wrong_input);
        loginButton = findViewById(R.id.bt_login_login);
        signupButton = findViewById(R.id.bt_login_signup);


    }

    private void attachListeners() {
        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);

        numberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (/*event != null && */actionId == EditorInfo.IME_ACTION_GO) {
                if (isInternetAvailable()) {
                    initializeDialog();
                    mProgressDialog.show();
                    proceedToLogin();

                } else {
                    Toast.makeText(this, "please check your internet connection", Toast.LENGTH_SHORT).show();
                }
                return false;//this will shut down the keyboard
            }
            return false;//this will shut down the keyboard
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login_login:
                if (isInternetAvailable()) {
                    initializeDialog();
                    mProgressDialog.show();
                    proceedToLogin();
                } else {
                    Toast.makeText(this, "please check your internet connection", Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.bt_login_signup:
                startActivity(new Intent(this, SignupActivity.class));
                finish();
                break;
        }
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mProgressDialog = new AlertDialog.Builder(this).setView(alertDialog).setCancelable(false).create();
    }

    private void proceedToLogin() {
        mPhoneNumber = "+" + countryCodePicker.getSelectedCountryCode() + numberEditText.getText().toString().trim();
        if (numberEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "plaese provide the number", Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
            return;
        }
        if (!PhoneNumberUtils.isGlobalPhoneNumber(mPhoneNumber)) {
            Toast.makeText(LoginActivity.this, "please eneter a valid  phone number in specified format", Toast.LENGTH_SHORT).show();
            wrongInputAlertTextView.setText("please eneter a valid  phone number in specified format");
            wrongInputAlertTextView.setVisibility(View.VISIBLE);
            mProgressDialog.dismiss();
            return;
        }
        if (wrongInputAlertTextView.isShown()) {
            wrongInputAlertTextView.setVisibility(View.GONE);
        }
        loginButton.setEnabled(false);
        signupButton.setEnabled(false);
        checkValidityAndAuthenticate(mPhoneNumber);


    }

    private void checkValidityAndAuthenticate(final String number) {
        rootRef.child("users")
                .orderByChild("number").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (LoginActivity.this != null) {
                    if (dataSnapshot.exists()) {
                        sendToPhoneAuhUI(number);

                    } else {
                        wrongInputAlertTextView.setText("the number provided is not registered please tap register to create an account");
                        wrongInputAlertTextView.setVisibility(View.VISIBLE);
                        mProgressDialog.dismiss();

                    }
                    loginButton.setEnabled(true);
                    signupButton.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (LoginActivity.this != null)
                    mProgressDialog.dismiss();
            }
        });
    }

    private void sendToPhoneAuhUI(String number) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder()
                .setDefaultNumber(number).build());
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), RC_LOGIN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_LOGIN) {
            if (resultCode == RESULT_OK) {
                sendUserToRelevantActiviy();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void sendUserToRelevantActiviy() {


        FirebaseUser curuntUser = FirebaseAuth.getInstance().getCurrentUser();
        if (curuntUser != null) {//now user is logged in
            //we check that user type and send the user to its respective activity
            rootRef.child("users").child("" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (LoginActivity.this != null) {
                        String userType = dataSnapshot.getValue(String.class);

                        if (userType != null && userType.equals(getResources().getString(R.string.provider))) {
                            //            showProgressBar(false);
                            startActivity(new Intent(LoginActivity.this, ProviderMainActivity.class));
                            SharedPreferenceUtil.storeValue(getApplicationContext(), getResources().getString(R.string.type_key), userType);
                            mProgressDialog.dismiss();
                            finish();
                        } else if (userType != null && userType.equals(getResources().getString(R.string.consumer))) {
                            //          showProgressBar(false);
                            startActivity(new Intent(LoginActivity.this, ConsumerMainActivity.class));
                            SharedPreferenceUtil.storeValue(getApplicationContext(), getResources().getString(R.string.type_key), userType);
                            mProgressDialog.dismiss();
                            finish();
                        } else if (userType != null && userType.equals(getResources().getString(R.string.transporter))) {
                            //          showProgressBar(false);
                            startActivity(new Intent(LoginActivity.this, TransporterMainActivity.class));
                            SharedPreferenceUtil.storeValue(getApplicationContext(), getResources().getString(R.string.type_key), userType);
                            mProgressDialog.dismiss();
                            finish();
                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //    showProgressBar(false);
                    mProgressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "some error accured please restart the application", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}