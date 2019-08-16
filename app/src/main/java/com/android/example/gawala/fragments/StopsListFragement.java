package com.android.example.gawala.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.example.gawala.Adapters.StopsMarkersAdapter;
import com.android.example.gawala.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Models.StopMarkerModel;
import com.android.example.gawala.R;

import java.util.ArrayList;


public class StopsListFragement extends Fragment implements StopMarkerClickCallBack {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MARKERS_KEY = "markersKey";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private ArrayList<StopMarkerModel> mStopsMarkerList;
    private String mParam2;
    private StopsMarkersAdapter mStopsMarkersAdapter;
    private RecyclerView mRecyclerView;


    private Callbacks callbacks;

    public void setCallBacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public StopsListFragement() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stopsMarkerList a list of serialized markers to be contained.
     * @param param2          Parameter 2.
     * @return A new instance of fragment StopsListFragement.
     */
    // TODO: Rename and change types and number of parameters
    public static StopsListFragement newInstance(ArrayList<StopMarkerModel> stopsMarkerList, String param2) {
        StopsListFragement fragment = new StopsListFragement();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_points_list_fragement, container, false);
        initFields(rootView);
        return rootView;
    }

    private void initFields(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.rv_frag_stop_markers_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStopsMarkersAdapter = new StopsMarkersAdapter(mStopsMarkerList, getActivity(), this);
        mRecyclerView.setAdapter(mStopsMarkersAdapter);

    }

    @Override
    public void onStopMarkerItemClick(int position) {
        callbacks.onStopMarkerItemClick(position); //call forwarded to activity
    }

    public interface Callbacks {
        void onStopMarkerItemClick(int position);
    }
}
