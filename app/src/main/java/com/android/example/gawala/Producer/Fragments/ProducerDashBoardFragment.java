package com.android.example.gawala.Producer.Fragments;


import android.content.DialogInterface;
import android.os.Bundle;

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

import com.android.example.gawala.Producer.Adapters.ConsumerMarkersAdapter;
import com.android.example.gawala.Producer.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.android.example.gawala.Producer.Utils.ProducerFirebaseHelper;


import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProducerDashBoardFragment extends Fragment implements StopMarkerClickCallBack {
    private TextView milkRateTextView, milkDemandTextView;
    private ImageButton editMilkRateImageButton;


    private static final String ARG_MARKERS_KEY = "markersKey";



    private Callbacks callbacks;


    private ArrayList<ConsumerModel> consumerModelArrayList;
    private ConsumerMarkersAdapter mConsumerMarkersAdapter;
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
            consumerModelArrayList = (ArrayList<ConsumerModel>) getArguments().getSerializable(ARG_MARKERS_KEY);
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
        milkRateTextView = rootView.findViewById(R.id.tv_frag_prod_dash_board_price);
        milkDemandTextView = rootView.findViewById(R.id.tv_frag_prod_dash_board_milk_demand);
        editMilkRateImageButton = rootView.findViewById(R.id.ib_frag_prod_dash_board_edit_price);

        mRecyclerView = rootView.findViewById(R.id.rv_frag_prod_dash_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mConsumerMarkersAdapter= new ConsumerMarkersAdapter(consumerModelArrayList, getActivity(), this);
        mRecyclerView.setAdapter(mConsumerMarkersAdapter);
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

                callbacks.onStartRiding();

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

/**
 * in dash boead what we need is
 * 1) an input from producer that what is milk rate
 * 2) amount of milk delivered this month
 * 3) list of connected consumers (can uise the same activity which is already there)
 * 4) on click of each consumer we can see the details that how much milk is delivered this month
 */

// TODO: 9/10/2019  c) milk amounct to deliver today will be calculated later wehn consumers is merged with he stop

// TODO: 10/3/2019 now we need to modify this app we have 3 days its not a difficult task do not add much details and also see the thesis a little to get the idea adn also add feasibility study plus thefinal chapter but this will be done ;later than the app modification and then poilish the app you can alos take help from other e commerce apps
//  3) Producer will choose from the enlisted items.