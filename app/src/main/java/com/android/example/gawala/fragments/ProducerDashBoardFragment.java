package com.android.example.gawala.fragments;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.example.gawala.Adapters.StopsMarkersAdapter;
import com.android.example.gawala.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Models.StopMarkerModel;
import com.android.example.gawala.R;
import com.android.example.gawala.Utils.Firebase.ProducerFirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProducerDashBoardFragment extends Fragment implements StopMarkerClickCallBack {
    private TextView milkRateTextView, milkDemandTextView;
    private ImageButton editMilkRateImageButton;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MARKERS_KEY = "markersKey";
    private static final String ARG_PARAM2 = "param2";


    private Callbacks callbacks;


    private ArrayList<StopMarkerModel> mStopsMarkerList;
    private String mParam2;
    private StopsMarkersAdapter mStopsMarkersAdapter;
    private RecyclerView mRecyclerView;

    private Button startRideButton;
    private float mTotalMilk=0;

    public ProducerDashBoardFragment() {
        // Required empty public constructor
    }

    /**
     * @param stopsMarkerList a list of serialized markers to be contained.
     * @param param2          Parameter 2.
     * @return
     */

    public static ProducerDashBoardFragment geInstance(ArrayList<StopMarkerModel> stopsMarkerList, String param2) {

        ProducerDashBoardFragment fragment = new ProducerDashBoardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MARKERS_KEY, stopsMarkerList);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStopsMarkerList = (ArrayList<StopMarkerModel>) getArguments().getSerializable(ARG_MARKERS_KEY);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_producer_dash_board, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Dash Board");
        initFields(rootView);
        attachListeners();
        captureMilkDemandToday();
        return rootView;
    }

    private void initFields(View rootView) {
        milkRateTextView = rootView.findViewById(R.id.tv_frag_prod_dash_board_price);
        milkDemandTextView = rootView.findViewById(R.id.tv_frag_prod_dash_board_milk_demand);
        editMilkRateImageButton = rootView.findViewById(R.id.ib_frag_prod_dash_board_edit_price);

        mRecyclerView = rootView.findViewById(R.id.rv_frag_prod_dash_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStopsMarkersAdapter = new StopsMarkersAdapter(mStopsMarkerList, getActivity(), this);
        mRecyclerView.setAdapter(mStopsMarkersAdapter);
        startRideButton = rootView.findViewById(R.id.bt_frag_prod_dash_board_start_ride);

    }

    private void attachListeners() {

        editMilkRateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForEditting();
            }
        });

        startRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                callbacks.onStartRiding(mTotalMilk);

            }
        });

    }

    private void showDialogForEditting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        builder.setTitle("Edit milk Price")
                .setMessage("please type the new price in the field")
                .setView(editText)
                .setPositiveButton("update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPrice = editText.getText().toString().trim();
                        ProducerFirebaseHelper.updateRate(newPrice);
                        milkRateTextView.setText("price : PKR " + newPrice + " per litre");
                    }
                }).setNegativeButton("cancel", null).show();

    }

    private void captureMilkDemandToday() {
        //logic
        //the consumers has already updted in fire base that eicther he milk is reqiuired or not along side that how much milk is required
        //so we fetch the available values of clients and calculte current days milk demand

        FirebaseDatabase.getInstance().getReference()
                .child("data")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//current  producer id
                .child("live_data").child("clients_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mTotalMilk=0;
                    for (DataSnapshot consumerSnap:dataSnapshot.getChildren()){
                        if (consumerSnap.hasChild("at_home") && consumerSnap.child("at_home").getValue(Boolean.class)){
                            if (consumerSnap.hasChild("milk_demand")){
                                    mTotalMilk += Float.parseFloat(consumerSnap.child("milk_demand").getValue(String.class));
                            }
                        }
                    }
                    milkDemandTextView.setText(mTotalMilk+" litre(s)");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public interface Callbacks {
        void onStopMarkerItemClick(int position);

        void onStartRiding(float mTotalMilk);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void setCallBacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onStopMarkerItemClick(int position) {
        callbacks.onStopMarkerItemClick(position); //call forwarded to ProucerNAv Main Activity activity
    }
}

/**
 * in dash boead what we need is
 * 1) an input from producer that what is milk rate
 * 2) amount of milk delivered this month
 * 3) list of connected consumers (can uise the same activity which is already there)
 * 4) on click of each consumer we can see the details that how much milk is delivered this month
 */

// TODO: 9/10/2019 now
//  3)on consumer side solve some minor bugs.

// TODO: 9/10/2019  c) milk amounct to deliver today will be calculated later wehn consumers is merged with he stop
// TODO: 9/10/2019  e) show a dialog taht asks athe this much amount of nilk to deliver in that much time are you ready?