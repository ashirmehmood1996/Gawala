package com.android.example.gawala.Utils.Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class ConsumerFirebaseHelper {

    private static DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private static String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private ConsumerFirebaseHelper() {
    }

    public static void updateMilkDemand(String litresOfMilk, String producerKey) {
        rootRef.child("data").child(producerKey)
                .child("live_data")
                .child("clients_data").child(myId).child("milk_demand")
                .setValue(litresOfMilk);
    }

    public static void atHome(boolean isAtHome, String producerKey) {
        rootRef.child("data").child(producerKey)
                .child("live_data")
                .child("clients_data").child(myId).child("at_home")
                .setValue(isAtHome);
    }

}
