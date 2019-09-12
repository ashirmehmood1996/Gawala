package com.android.example.gawala.Utils.Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class ProducerFirebaseHelper {
    private static DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    private ProducerFirebaseHelper() {
    }


    /**
     * to update new milk selling price per litre
     * @param milkRate new price as string
     */
    public static void updateRate(String milkRate) {
        rootRef.child("data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("live_data")
                .child("milk_price").setValue(milkRate);
    }

    /**
     * to update status of user for clients
     * @param status new status
     */
    public static void updateStatus(String status) {
        rootRef.child("data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("live_data")
                .child("status").setValue(status);
    }


}
// TODO: 8/25/2019 for now the live data is uodated any time the user updates it later we will keep record of all the data at least for a year or two for statistical analysis