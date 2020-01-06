package com.android.example.gawala.Generel.Fraagments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Generel.Activities.ProfileActivity;
import com.android.example.gawala.Generel.Models.RatingModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class RatingFragment extends DialogFragment {

    private static final String OTHER_ID = "other_id";

    //UI reltaed
    private ImageButton editRatingBarImageButton;
    private RatingBar myRatingBar;
    private TextView ratingDescriptionTextView;

    private RecyclerView ratingRecyclerView;
    private RatingAdapter ratingAdapter;
    private ArrayList<RatingModel> ratingModelArrayList;


    //data
    private String otherID;
    private String myId;
    private RatingModel myratingModel;

    private boolean isRatedAlready = false;
    private float overAllRating, totalNumberOfratings, averageRating;//over all is simple addition of all stars , number of rating is the number of all users that rated and average result is actual rating
    private Callbacks callbacks;


    public RatingFragment() {

    }

    public static RatingFragment newInstance(String otherId) {
        RatingFragment ratingFragment = new RatingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(OTHER_ID, otherId);
        ratingFragment.setArguments(bundle);
        return ratingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
        if (getArguments() != null) {
            otherID = getArguments().getString(OTHER_ID, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_rating, container, false);
        initFields(rootView);
        attachListeners();
        loadMyRatings();
        loadOverAllRatingSummery();
        loadAllRatings();
        return rootView;
    }

    private void initFields(View rootView) {
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editRatingBarImageButton = rootView.findViewById(R.id.ib_frag_rating_edit_mine);
        editRatingBarImageButton.setEnabled(false);
        myRatingBar = rootView.findViewById(R.id.rb_frag_rating_mine);
        ratingDescriptionTextView = rootView.findViewById(R.id.tv_frag_rating_desc_mine);

        ratingRecyclerView = rootView.findViewById(R.id.rv_frag_rating);
        ratingModelArrayList = new ArrayList<>();
        ratingAdapter = new RatingAdapter(getActivity(), ratingModelArrayList);
        ratingRecyclerView.setAdapter(ratingAdapter);

        rootView.findViewById(R.id.ib_frag_rating_back).setOnClickListener(v -> dismiss());
    }

    private void attachListeners() {
        editRatingBarImageButton.setOnClickListener(this::showRatingDialog);
    }

    private void showRatingDialog(View v) {

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_rating, null);
        RatingBar ratingBar = linearLayout.findViewById(R.id.rb_dialog_rating);

        EditText descEditText = linearLayout.findViewById(R.id.et_dialog_rating);

        if (myratingModel != null) {
            ratingBar.setRating(Float.parseFloat(myratingModel.getRating()));
            descEditText.setText(myratingModel.getDescription());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(linearLayout);


        builder.setPositiveButton("submit", (dialog, which) -> {

            String rating = ratingBar.getRating() + "";
            String description = descEditText.getText().toString().trim();

            if (isRatedAlready) {
                float oldRating = Float.parseFloat(myratingModel.getRating());
                updateOverAllRatinginFirebase(rating, oldRating);
            } else {
                updateOverAllRatinginFirebase(rating, 0.0f);
            }

            sendNewRatingToFirebase(rating, description);


        });
        builder.setNegativeButton("cancel", null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        descEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                descEditText.removeTextChangedListener(this);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        alertDialog.setOnShowListener(dialog -> {
//
//            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//        });

        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if (fromUser) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }
        });

    }

    private void updateOverAllRatinginFirebase(String newRAting, float oldRating) {
        float ratingFloat = Float.parseFloat(newRAting);
        overAllRating = overAllRating - oldRating + ratingFloat;
        if (!isRatedAlready) {//we need to increment the rating count if we havent rated already
            totalNumberOfratings++;
        }
        averageRating = overAllRating / totalNumberOfratings;
        callbacks.onRatingChanged(averageRating);
        HashMap<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("over_all", overAllRating);
        ratingMap.put("total_number", totalNumberOfratings);
        rootRef.child("users").child(otherID).child("rating").updateChildren(ratingMap);
    }

    private void sendNewRatingToFirebase(String rating, String desc) {

        DatabaseReference ratingRef = rootRef.child("ratings")
                .child(otherID)//raing reciever id
                .child(myId);//rater id

        if (desc.trim().isEmpty()) {
            desc = "User didn't provide remarks.";
        }
        RatingModel ratingModel = new RatingModel(rating, otherID, Calendar.getInstance().getTimeInMillis() + "",
                myId, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), desc);


        ratingRef.setValue(ratingModel).addOnCompleteListener(task -> {
            myratingModel = ratingModel;
            myRatingBar.setRating(Float.parseFloat(myratingModel.getRating()));
            ratingDescriptionTextView.setText(myratingModel.getDescription());

            if (isRatedAlready) {
                for (int i = 0; i < ratingModelArrayList.size(); i++) {
                    RatingModel ratingModel1 = ratingModelArrayList.get(i);
                    if (ratingModel1.getRater_id().equals(myId)) {
                        ratingModel1.update(myratingModel);
                        ratingAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            } else {
                ratingModelArrayList.add(myratingModel);
                ratingAdapter.notifyItemChanged(ratingModelArrayList.indexOf(myratingModel));
            }
            isRatedAlready = true;//because now its rated
            Toast.makeText(getActivity(), "rating updated successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMyRatings() {

        ratingDescriptionTextView.setText("loading...");

        rootRef.child("ratings").child(otherID)
                .child(myId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            myratingModel = dataSnapshot.getValue(RatingModel.class);
                            myRatingBar.setRating(Float.parseFloat(myratingModel.getRating()));
                            ratingDescriptionTextView.setText(myratingModel.getDescription());
                            isRatedAlready = true;

                        } else {
                            ratingDescriptionTextView.setText("Not rated yet click edit button to rate");
                            isRatedAlready = false;
                        }
                        editRatingBarImageButton.setEnabled(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        ratingDescriptionTextView.setText("error while laoding");

                    }
                });
    }

    private void loadOverAllRatingSummery() {
        rootRef.child("users").child(otherID).child("rating")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            overAllRating = dataSnapshot.child("over_all").getValue(Float.class);
                            totalNumberOfratings = dataSnapshot.child("total_number").getValue(Float.class);
                            averageRating = overAllRating / totalNumberOfratings;

                        } else {
                            overAllRating = 0.0f;
                            totalNumberOfratings = 0.0f;
                            averageRating = overAllRating / totalNumberOfratings;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadAllRatings() {

        rootRef.child("ratings").child(otherID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ratingModelArrayList.clear();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot ratingSnapshot : dataSnapshot.getChildren()) {
                                RatingModel ratingModel = ratingSnapshot.getValue(RatingModel.class);
                                ratingModelArrayList.add(ratingModel);
                                ratingAdapter.notifyItemInserted(ratingModelArrayList.indexOf(ratingModel));//change if causes problems and take help from video downloader or other previously developed apps
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }


    private class RatingAdapter extends RecyclerView.Adapter<RatingHolder> {
        private Context context;
        private ArrayList<RatingModel> ratingModelArrayList;

        public RatingAdapter(Context context, ArrayList<RatingModel> ratingModelArrayList) {
            this.context = context;
            this.ratingModelArrayList = ratingModelArrayList;
        }

        @NonNull
        @Override
        public RatingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


            return new RatingHolder(LayoutInflater.from(context).inflate(R.layout.li_rating, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RatingHolder holder, int position) {
            RatingModel ratingModel = ratingModelArrayList.get(position);
            long timeInMillis = Long.parseLong(ratingModel.getTime_stamp());
            holder.dateTextView.setText(new SimpleDateFormat("dd/MM/YY").format(timeInMillis));//fixme LATER deal woth the warning by studying on internet in details
            holder.nameTextView.setText(ratingModel.getRater_name());
            holder.descTextView.setText(ratingModel.getDescription());
            holder.ratingBar.setRating(Float.parseFloat(ratingModel.getRating()));
        }

        @Override
        public int getItemCount() {
            return ratingModelArrayList.size();
        }
    }

    private class RatingHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView descTextView, dateTextView, nameTextView;

        RatingHolder(@NonNull View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.rb_li_rating);
            nameTextView = itemView.findViewById(R.id.tv_li_rating_rater_name);
            dateTextView = itemView.findViewById(R.id.tv_li_rating_date);
            descTextView = itemView.findViewById(R.id.tv_li_rating_desc);
        }
    }

    public interface Callbacks {
        void onRatingChanged(float averageRating);
    }
}

//    a user
//    shoud not
//    be able
//    to rate
//    its self
//    may be yes