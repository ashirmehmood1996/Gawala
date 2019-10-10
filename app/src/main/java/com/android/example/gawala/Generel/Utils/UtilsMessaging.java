package com.android.example.gawala.Generel.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public final class UtilsMessaging {
    private UtilsMessaging() {
    }

    //send the current device token associated with the current user id token to the database
    public static void initFCM() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getToken();
                            sendRegistrationToServer(token);
                        } else {
                            Log.d("FireabseMessagingUtil", "initFCM: token fetching error: " + task.getException().getMessage());
                        }
                    }
                });


    }

    public static void sendRegistrationToServer(String token) {
        //Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userIDNodeRef = reference.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userIDNodeRef.child("messaging_token").setValue(token);//adding device token to user data



    }

    public static void deleteRegistrationFromServer() {
        //Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("messaging_token").removeValue();
    }


}
