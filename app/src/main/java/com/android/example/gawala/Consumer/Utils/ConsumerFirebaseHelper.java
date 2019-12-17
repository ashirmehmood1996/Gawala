package com.android.example.gawala.Consumer.Utils;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.google.firebase.auth.FirebaseAuth;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public final class ConsumerFirebaseHelper {

    private static String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private ConsumerFirebaseHelper() {
    }


    public static void updateDemand(String demand, AcquiredGoodModel acquiredGoodModel) {
        rootRef.child("demand").child(acquiredGoodModel.getProducerId())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//consumer id
                .child(acquiredGoodModel.getGoodModel().getId())//good id
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
