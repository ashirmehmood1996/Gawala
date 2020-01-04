package com.android.example.gawala.Consumer.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import com.android.example.gawala.Consumer.Activities.AggressiveNotificationAlertActivity;
import com.android.example.gawala.Generel.Activities.MainActivity;
import com.android.example.gawala.Generel.App;
import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;


public class ClientsSettingsFragment extends DialogFragment {
    private static final String ALERT_NOTIFICATION_TIME = "alert_notification_time";
    public  static final String ALERT_NOTIFICATION_TYPE = "alert_notification_type";
    //private static String USER_ID = "userIdKey";

    private long alertNotificationTime = 2;// in minutes
    private boolean isTypeAggressive;//if true then aggressive else simple notification


    private ImageButton backImageButton, editNotificationTimeImageButton, editNotificationTypeImageButton, previewNotificationImageButton;
    private TextView timeDescriptionTextView, typeDescriptionTextView;
    private String myID;


    public static ClientsSettingsFragment getInstance(/*String userId*/) {
//        Bundle bundle = new Bundle();
//        bundle.putString(USER_ID, userId);
        ClientsSettingsFragment clientsSettingsFragment = new ClientsSettingsFragment();
//        producerSettingsFragment.setArguments(bundle);
        return clientsSettingsFragment;
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
        View rootView = inflater.inflate(R.layout.fragment_clients_settings, container, false);
        initFields(rootView);
        attachListeners();


        loadNotificationSettings();
        return rootView;

    }

    private void initFields(View rootView) {
        backImageButton = rootView.findViewById(R.id.ib_frag_client_settings_back);
        editNotificationTimeImageButton = rootView.findViewById(R.id.ib_frag_client_settings_edit_notification_time);
        editNotificationTypeImageButton = rootView.findViewById(R.id.ib_frag_client_settings_edit_notification_type);
        previewNotificationImageButton = rootView.findViewById(R.id.ib_frag_client_settings_preview);
        timeDescriptionTextView = rootView.findViewById(R.id.tv_frag_client_settings_notification_time);
        typeDescriptionTextView = rootView.findViewById(R.id.tv_frag_client_settings_notification_type);

        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }


    private void attachListeners() {

        editNotificationTimeImageButton.setOnClickListener(this::onClick);
        editNotificationTypeImageButton.setOnClickListener(this::onClick);
        previewNotificationImageButton.setOnClickListener(this::onClick);
        backImageButton.setOnClickListener(v -> dismiss());

    }

    private void loadNotificationSettings() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SharedPreferenceUtil.GAWALA_PREF, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(ALERT_NOTIFICATION_TIME)) {//if its there already then ok
            alertNotificationTime = sharedPreferences.getLong(ALERT_NOTIFICATION_TIME, 2);
            isTypeAggressive = sharedPreferences.getBoolean(ALERT_NOTIFICATION_TYPE, false);
            updateUI();
        } else { /// other wise its the first visit after login

            rootRef.child("users").child(myID).child("notification_prefs")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) { // then user had already changed the preferences
                                alertNotificationTime = dataSnapshot.child(ALERT_NOTIFICATION_TIME).getValue(Long.class);
                                isTypeAggressive = dataSnapshot.child(ALERT_NOTIFICATION_TYPE).getValue(Boolean.class);

                            } else {//user never changed the preferences hence use the defaults

                            }
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong(ALERT_NOTIFICATION_TIME, alertNotificationTime);
                            editor.putBoolean(ALERT_NOTIFICATION_TYPE, isTypeAggressive);
                            editor.commit();
                            updateUI();
//                            store the pref in shared preferences and use them unless and untill the rpefs are changed by the user\
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }

    }

    private void updateUI() {
        timeDescriptionTextView.setText(Html.fromHtml("Notify me abouty <b>" + alertNotificationTime + " minutes</b> before delivery"));
        if (isTypeAggressive) {//if treu then aggressive
            typeDescriptionTextView.setText("Aggressive");
        } else {
            typeDescriptionTextView.setText("Normal");
        }
    }

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_frag_client_settings_edit_notification_time:
                showMinutesEditDialog();
                break;

            case R.id.ib_frag_client_settings_edit_notification_type:
                showTypeOptionsAlerDialog();
                break;
            case R.id.ib_frag_client_settings_preview:

                showPreviewNotification();
                if (isTypeAggressive) {
                    showAggressiveActivity();

                }
                break;

        }

    }


    private void showPreviewNotification() {
        //creating an intent and passing it through a pending intent which will be called when notification is clicked
        Intent activityIntent = new Intent(getActivity(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(//we can also call getService or broadcast reciever etc
                getActivity(), //context
                0, //id for pending intent if we want to cancel it later
                activityIntent, //intent to be executed by the notification
                0);
        //creating a notification
        NotificationCompat.Builder downloadNotificationBuilder;
        downloadNotificationBuilder = new NotificationCompat.Builder(getActivity(), App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_add_location_black_24dp)
                .setContentTitle("Demo Notification")
                .setContentText("Demo description of notification")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true);

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(11, downloadNotificationBuilder.build());

    }


    private void showAggressiveActivity() {
        Intent intent = new Intent(getActivity(), AggressiveNotificationAlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AggressiveNotificationAlertActivity.IS_DEMO, true);
        startActivity(intent);
    }


    private void showMinutesEditDialog() {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_edit_units, null);
        TextView amountLitresTextView = linearLayout.findViewById(R.id.tv_dialog_edit_unit);
        amountLitresTextView.setText(alertNotificationTime + " minute(s) ");
        ImageButton addImageButton = linearLayout.findViewById(R.id.ib_dialog_edit_units_add);
        addImageButton.setOnClickListener(v -> {
            if (alertNotificationTime < 30) {
                alertNotificationTime += 1;
                amountLitresTextView.setText(alertNotificationTime + " minute(s) ");
            } else {


                ObjectAnimator colorAnim = ObjectAnimator.ofInt(amountLitresTextView, "textColor",
                        Color.RED, Color.GRAY);
                colorAnim.setEvaluator(new ArgbEvaluator());
                colorAnim.setDuration(1000);
                colorAnim.start();
            }
        });
        ImageButton reduceImageButton = linearLayout.findViewById(R.id.ib_dialog_edit_units_remove);
        reduceImageButton.setOnClickListener(v -> {
            if (alertNotificationTime > 1) {
                alertNotificationTime -= 1;
                amountLitresTextView.setText(alertNotificationTime + " minute(s) ");
            } else {
                ObjectAnimator colorAnim = ObjectAnimator.ofInt(amountLitresTextView, "textColor",
                        Color.RED, Color.GRAY);
                colorAnim.setEvaluator(new ArgbEvaluator());
                colorAnim.setDuration(1000);
                colorAnim.start();
            }
        });

        new AlertDialog.Builder(getActivity())
                .setView(linearLayout)
                .setPositiveButton("ok", (dialog, which) -> {
                    updateUI();

                    uploadNewSettingsTodatabase();
                    dialog.dismiss();
                })
                .show();
    }

    private void uploadNewSettingsTodatabase() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(SharedPreferenceUtil.GAWALA_PREF, Context.MODE_PRIVATE).edit();
        HashMap<String, Object> notificationSettingsMap = new HashMap<>();
        notificationSettingsMap.put(ALERT_NOTIFICATION_TIME, alertNotificationTime);
        notificationSettingsMap.put(ALERT_NOTIFICATION_TYPE, isTypeAggressive);
        rootRef.child("users").child(myID).child("notification_prefs").updateChildren(notificationSettingsMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() /*&& getActivity() != null*/) {

                        editor.putLong(ALERT_NOTIFICATION_TIME, alertNotificationTime);
                        editor.putBoolean(ALERT_NOTIFICATION_TYPE, isTypeAggressive);
                        editor.commit();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "data updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showTypeOptionsAlerDialog() {

        // setup the alert builder
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Notification Type");
// add a radio button list
        String[] unitArray = getResources().getStringArray(R.array.alert_notification_type);
        final int[] checkedItem = {0};// automattically converted to a single item array for final variable issues
        if (isTypeAggressive) {
            checkedItem[0] = 1;
        }

        builder.setSingleChoiceItems(unitArray, checkedItem[0], (dialog, which) -> {

            checkedItem[0] = which;
            // user checked an item
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (checkedItem[0] == 0) {
                isTypeAggressive = false;
            } else {
                isTypeAggressive = true;
            }
            updateUI();
            uploadNewSettingsTodatabase();
            // user clicked OK
        });
        builder.setNegativeButton("Cancel", null);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

    }
}
//here
//alert activity- makng it happen when  the provider sends it and also make it happen only once whihc will require to maintain aboolean field in user model class i guess