package com.android.example.gawala.Consumer.Utils;

import com.android.example.gawala.Consumer.Models.AquiredGoodModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class ConsumerFirebaseHelper {

    private static DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private static String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private ConsumerFirebaseHelper() {
    }


    public static void updateDemand(String demand, AquiredGoodModel aquiredGoodModel) {
        rootRef.child("demand").child(aquiredGoodModel.getProducerId())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//consumer id
                .child(aquiredGoodModel.getGoodModel().getId())//good id
                .child("demand").setValue(demand);
    }

    public static void atHome(boolean isAtHome, String producerKey) {
        rootRef.child("clients")
                .child(producerKey)
                .child(myId)
                .child("live_data")
                .child("at_home")
                .setValue(isAtHome);
    }

}
