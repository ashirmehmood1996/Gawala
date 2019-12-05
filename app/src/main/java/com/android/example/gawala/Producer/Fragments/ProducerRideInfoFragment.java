package com.android.example.gawala.Producer.Fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Producer.Adapters.ConsumerMarkersAdapter;
import com.android.example.gawala.Producer.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProducerRideInfoFragment extends DialogFragment implements StopMarkerClickCallBack {
    private static final String ARG_MARKERS_KEY = "markersKey";

    private Callbacks callbacks;

    private ArrayList<ConsumerModel> mActiveRidesArrayList;
    private ConsumerMarkersAdapter mConsumerMarkersAdapter;
    private RecyclerView mRecyclerView;

    private LinearLayout goodsToCarryContainerLinearLayout;

    private Button startRideButton;
    private ImageButton backButton;

    private ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList;
    private HashMap<String, Object> goodsToCarryHashMap;

    public ProducerRideInfoFragment() {
        // Required empty public constructor
    }

    /**
     * @param aactiveRidesArrayList a list of serialized markers to be contained.
     * @return
     */

    public static ProducerRideInfoFragment geInstance(ArrayList<ConsumerModel> aactiveRidesArrayList) {


        ArrayList<ConsumerModel> activeRidesArrayList = new ArrayList<>();
        for (ConsumerModel consumerModel : aactiveRidesArrayList) {
            activeRidesArrayList.add(consumerModel.getConsumerModel());
        }

        ProducerRideInfoFragment fragment = new ProducerRideInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MARKERS_KEY, activeRidesArrayList);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
        if (getArguments() != null) {
            mActiveRidesArrayList = (ArrayList<ConsumerModel>) getArguments().getSerializable(ARG_MARKERS_KEY);


            acquiredGoodModelArrayList = new ArrayList<>();

            for (ConsumerModel consumerModel : mActiveRidesArrayList) {
//                consumerModel.setMarker(null);// making it null to avoid serialization issue
                consumerModel.getDemandArray();
                String itemName = "";
                acquiredGoodModelArrayList.addAll(consumerModel.getDemandArray());
                LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.li_good_to_carry, null);
                TextView nameTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_name);
                TextView totalUnitsTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_units);

            }
            goodsToCarryHashMap = new HashMap<>();
            for (AcquiredGoodModel acquiredGoodModel : acquiredGoodModelArrayList) {
                String key = acquiredGoodModel.getGoodModel().getName();

                if (goodsToCarryHashMap.containsKey(key)) {
                    HashMap<String, Object> subMap = (HashMap<String, Object>) goodsToCarryHashMap.get(key);
                    Integer previousDemand = (Integer) subMap.get("demand");
                    Integer newDemand = Integer.parseInt(acquiredGoodModel.getDemand());
                    Integer totalDemand = previousDemand + newDemand;
                    subMap.put("demand", totalDemand);

                } else {
                    HashMap<String, Object> subMap = new HashMap<>();
                    subMap.put("name", acquiredGoodModel.getGoodModel().getName());
                    subMap.put("image", acquiredGoodModel.getGoodModel().getImage_uri());
                    subMap.put("demand", Integer.parseInt(acquiredGoodModel.getDemand()));
                    goodsToCarryHashMap.put(key, subMap);
                }
            }


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_producer_dash_board, container, false);
        initFields(rootView);
        attachListeners();
//        captureMilkDemandToday();
//        showDemandList();
        return rootView;
    }


    private void initFields(View rootView) {

        mRecyclerView = rootView.findViewById(R.id.rv_frag_prod_dash_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mConsumerMarkersAdapter = new ConsumerMarkersAdapter(mActiveRidesArrayList, getActivity(), this);
        mRecyclerView.setAdapter(mConsumerMarkersAdapter);
        startRideButton = rootView.findViewById(R.id.bt_frag_prod_dash_board_start_ride);
        backButton = rootView.findViewById(R.id.iv_frag_prod_dash_board_back);

        goodsToCarryContainerLinearLayout = rootView.findViewById(R.id.ll_frag_prod_dash_board_demand_container);


    }

    private void attachListeners() {

        backButton.setOnClickListener(v -> dismiss());
        startRideButton.setOnClickListener(v ->
        {
            callbacks.onStartRiding(goodsToCarryHashMap);
            dismiss();
        });

    }

//    private void showDemandList() {
//        for (String key : goodsToCarryHashMap.keySet()) {
//            HashMap<String, Object> subMap = (HashMap<String, Object>) goodsToCarryHashMap.get(key);
//            String name = (String) subMap.get("name");
//            String image = (String) subMap.get("image");
//            Integer demand = (Integer) subMap.get("demand");
//            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.li_good_to_carry, null);
//
//            ImageView imageView = linearLayout.findViewById(R.id.iv_li_good_to_carry_image);
//            TextView nameTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_name);
//            TextView demandTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_units);
//            Glide.with(getActivity()).load(image).into(imageView);
//            nameTextView.setText(name);
//            demandTextView.setText(String.format("%d item(s)", demand));
//            goodsToCarryContainerLinearLayout.addView(linearLayout);
//        }
//    }

    public interface Callbacks {
        void onStopMarkerItemClick(int position );

        void onStartRiding(HashMap<String, Object> goodsToCarryHashMap);
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


//    show the each type of items akong with respective amount that is needed to
//    be carried our for the journy and then move to next task all data is there
//    in array list just need to organize a little the goods type can be differencited
//    on the bases of ids so we need just to make the logic
