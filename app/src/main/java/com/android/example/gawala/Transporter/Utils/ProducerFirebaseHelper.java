package com.android.example.gawala.Transporter.Utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public final class ProducerFirebaseHelper {

    private ProducerFirebaseHelper() {
    }


    /**
     * to update new milk selling price per litre
     *
     * @param milkRate new price as string
     */
    public static void updateRate(String milkRate) {
        rootRef.child("data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("live_data")
                .child("milk_price").setValue(milkRate);
    }

    /**
     * to update status of user for clients
     *
     * @param status new status
     */
    public static void updateStatus(String status) {
        rootRef.child("data").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("live_data")
                .child("status").setValue(status);
    }

    public static void addNewGood(GoodModel goodModel, final Activity activity) {
        DatabaseReference currentGoogRef = rootRef.child("goods")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .push();
        String good_id = currentGoogRef.getKey();
        goodModel.setId(good_id);
        currentGoogRef.setValue(goodModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (activity != null) {
                    Toast.makeText(activity, "product added successfully", Toast.LENGTH_SHORT).show();
                    activity.finish();
                }

            } else {
                if (activity != null)
                    Toast.makeText(activity, "Some error accured, product was not added", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static void updateGood(GoodModel goodModel, final Context context, String good_id) {
        rootRef.child("Goods")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(good_id).setValue(goodModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "product added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "product was not added", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
// TODO: 8/25/2019 for now the live data is uodated any time the user updates it later we will keep record of all the data at least for a year or two for statistical analysis