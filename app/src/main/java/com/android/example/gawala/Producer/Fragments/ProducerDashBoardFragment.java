package com.android.example.gawala.Producer.Fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.example.gawala.Producer.Adapters.ConsumerMarkersAdapter;
import com.android.example.gawala.Producer.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.R;


import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProducerDashBoardFragment extends Fragment implements StopMarkerClickCallBack {
    private static final String ARG_MARKERS_KEY = "markersKey";

    private Callbacks callbacks;

    private ArrayList<ConsumerModel> consumerModelArrayList;
    private ConsumerMarkersAdapter mConsumerMarkersAdapter;
    private RecyclerView mRecyclerView;

    private Button startRideButton;

    public ProducerDashBoardFragment() {
        // Required empty public constructor
    }

    /**
     * @param stopsMarkerList a list of serialized markers to be contained.
     * @param param2          Parameter 2.
     * @return
     */

    public static ProducerDashBoardFragment geInstance(ArrayList<ConsumerModel> stopsMarkerList, String param2) {

        ProducerDashBoardFragment fragment = new ProducerDashBoardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MARKERS_KEY, stopsMarkerList);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ArrayList<ConsumerModel> recievedconsumerModelArrayList = (ArrayList<ConsumerModel>) getArguments().getSerializable(ARG_MARKERS_KEY);
            consumerModelArrayList=new ArrayList<>();
            for (ConsumerModel consumerModel:recievedconsumerModelArrayList){
                if (consumerModel.hasDemand()){
                    consumerModelArrayList.add(consumerModel);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_producer_dash_board, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Dash Board");
        initFields(rootView);
        attachListeners();
//        captureMilkDemandToday();
        return rootView;
    }

    private void initFields(View rootView) {
//        milkRateTextView = rootView.findViewById(R.id.tv_frag_prod_dash_board_price);
//        milkDemandTextView = rootView.findViewById(R.id.tv_frag_prod_dash_board_milk_demand);
//        editMilkRateImageButton = rootView.findViewById(R.id.ib_frag_prod_dash_board_edit_price);

        mRecyclerView = rootView.findViewById(R.id.rv_frag_prod_dash_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mConsumerMarkersAdapter= new ConsumerMarkersAdapter(consumerModelArrayList, getActivity(), this);
        mRecyclerView.setAdapter(mConsumerMarkersAdapter);
        startRideButton = rootView.findViewById(R.id.bt_frag_prod_dash_board_start_ride);

    }

    private void attachListeners() {

//        editMilkRateImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialogForEditting();
//            }
//        });

        startRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                callbacks.onStartRiding();

            }
        });

    }

//    private void showDialogForEditting() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        final EditText editText = new EditText(getActivity());
//        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
//
//        builder.setTitle("Edit milk Price")
//                .setMessage("please type the new price in the field")
//                .setView(editText)
//                .setPositiveButton("update", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String newPrice = editText.getText().toString().trim();
//                        ProducerFirebaseHelper.updateRate(newPrice);
//                        milkRateTextView.setText("price : PKR " + newPrice + " per litre");
//                    }
//                }).setNegativeButton("cancel", null).show();
//
//    }



    public interface Callbacks {
        void onStopMarkerItemClick(int position);

        void onStartRiding();
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


