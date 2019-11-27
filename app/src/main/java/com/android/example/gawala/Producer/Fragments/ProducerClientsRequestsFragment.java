package com.android.example.gawala.Producer.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Producer.Adapters.ConnectedConsumersAdapter;
import com.android.example.gawala.Producer.Adapters.RequestsAdapter;
import com.android.example.gawala.Producer.Interfaces.RequestsAdapterCallbacks;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.Producer.Models.RequestModel;
import com.android.example.gawala.Producer.Activities.ProducerNavMapActivity;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ProducerClientsRequestsFragment extends Fragment implements RequestsAdapterCallbacks/*, ConnectedConsumersAdapter.CallBacks*/ {

    private static final String DIALOG_REQUEST_DETAILS = "dialog_request_details";
    //    private static String ARG_CONSUMERS_KEY ="consumersKey";
    private TextView newRequestsTitleTextView;
    private RecyclerView requestsRecyclerView;
    private ArrayList<RequestModel> requestModelArrayList;
    private RequestsAdapter requestsAdapter;

    private RecyclerView connectedConsumersRecyclerView;
    private ArrayList<ConsumerModel> mConsumersArrayList;
    private ConnectedConsumersAdapter consumersAdapter;


    private DatabaseReference rootRef;
    private String myID;

//    private CallBacks callBacks;

    public static ProducerClientsRequestsFragment geInstance() {

        ProducerClientsRequestsFragment fragment = new ProducerClientsRequestsFragment();
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Clients");
        View rootView = inflater.inflate(R.layout.fragment_producer_requests, container, false);
        initfields(rootView);
        loadRequests();
        return rootView;
    }

    private void initfields(View rootView) {
        //dataBase related
        rootRef = FirebaseDatabase.getInstance().getReference();
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //requests related
        newRequestsTitleTextView = rootView.findViewById(R.id.tv_pro_new_requests);
        requestsRecyclerView = rootView.findViewById(R.id.rv_pro_request);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestModelArrayList = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(requestModelArrayList, getActivity(), this);
        requestsRecyclerView.setAdapter(requestsAdapter);

        //connected consumers related
        connectedConsumersRecyclerView = rootView.findViewById(R.id.rv_pro_consumers);
        connectedConsumersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mConsumersArrayList = ((ProducerNavMapActivity) getActivity()).getConsumersArrayList();
        consumersAdapter = new ConnectedConsumersAdapter(mConsumersArrayList, getActivity()/*,this*/);
        connectedConsumersRecyclerView.setAdapter(consumersAdapter);


    }


    private void loadRequests() {
        requestModelArrayList.clear();
        rootRef.child("requests").child(myID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() & ProducerClientsRequestsFragment.this != null) {//then there are a number of requests
                            for (DataSnapshot requestSnap : dataSnapshot.getChildren()) {
                                String senderID = requestSnap.getKey();
                                String name = requestSnap.child("name").getValue(String.class);
                                String number = requestSnap.child("number").getValue(String.class);
                                String timeStamp = requestSnap.child("time_stamp").getValue(String.class);
                                String imageUrl = "";
                                if (requestSnap.hasChild("profile_image_uri")) {
                                    imageUrl = requestSnap.child("profile_image_uri").getValue(String.class);
                                }
                                // TODO: 11/23/2019  get the user image uri either in request or may be from storage refereecne directly using the id
                                String lat = requestSnap.child("lat").getValue(String.class);
                                String lng = requestSnap.child("lng").getValue(String.class);
                                requestModelArrayList.add(new RequestModel(senderID, name, number, timeStamp, lat, lng, imageUrl));
                            }
                            requestsAdapter.notifyDataSetChanged();
                        } else {
                            newRequestsTitleTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    @Override
    public void onRequestCancel(int position) {
        final RequestModel requestModel = requestModelArrayList.get(position);
        removeRequestNode(requestModel.getSender_id(), false, position);

    }

    @Override
    public void onRequestAccepted(final int position) {


        final RequestModel requestModel = requestModelArrayList.get(position);


        HashMap<String, Object> clientMap = new HashMap<>();

//        String name = requestModel.getName();
//        String number = requestModel.getNumber();
//        String time = requestModel.getTime_stamp(); //time when the request was sent
//        clientMap.put("name", name);
//        clientMap.put("number", number);
//        clientMap.put("time_stamp",time); //this time stamp is the time of sending this request
        String time_accept = Calendar.getInstance().getTimeInMillis() + "";
        clientMap.put("time_stamp", time_accept);
        clientMap.put("client_id", requestModel.getSender_id());// for datbase query later
        FirebaseDatabase.getInstance().getReference()
                .child("clients").child(myID)
                .child(requestModel.getSender_id())
                .setValue(clientMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addConenctedProducerNodeToDatabase(requestModel.getSender_id());
                        removeRequestNode(requestModel.getSender_id(), true, position);

                    } else {
                        Toast.makeText(getContext(), "something went wrong try later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequetClientClicked(int position) {
        showRequestDeltails(requestModelArrayList.get(position));
    }

    private void showRequestDeltails(RequestModel requestModel) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        ClientInfoFullScreenDialogFragment clientInfoFullScreenDialogFragment = (ClientInfoFullScreenDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_REQUEST_DETAILS);
        if (clientInfoFullScreenDialogFragment != null) {
            fragmentTransaction.remove(clientInfoFullScreenDialogFragment);
        }
        ClientInfoFullScreenDialogFragment dialogFragment = ClientInfoFullScreenDialogFragment.getInstance(requestModel);
//        dialogFragment.setCallback(this);
        dialogFragment.show(fragmentTransaction, DIALOG_REQUEST_DETAILS);

    }

    private void addConenctedProducerNodeToDatabase(String sender_id) {
        HashMap<String, Object> producerMap = new HashMap<>();
        producerMap.put("number", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        producerMap.put("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        rootRef.child("connected_producers")
                .child(sender_id).child(myID)
                .setValue(producerMap);
    }

    private void removeRequestNode(String sender_id, final boolean isAccepted, final int position) {
        FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(myID)
                .child(sender_id).removeValue().addOnCompleteListener(task -> {
            if (ProducerClientsRequestsFragment.this != null) {
                if (task.isSuccessful()) {
                    if (isAccepted) {
                        Toast.makeText(getContext(), "ClientSummery added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "request removed successfully", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getContext(), "something went wrong try later", Toast.LENGTH_SHORT).show();
                }
                requestModelArrayList.remove(position);
                requestsAdapter.notifyItemRemoved(position);
                requestsAdapter.notifyItemRemoved(position);
                requestsAdapter.notifyItemRangeChanged(position, requestModelArrayList.size());
                if (requestModelArrayList.isEmpty()) {
                    newRequestsTitleTextView.setVisibility(View.GONE);
                }
            }
        });

    }
/*
    @Override
    public void onEditLocation(int position) {

//        Toast.makeText(getActivity(), String.format("Edit location at %d postion clicked ", position), Toast.LENGTH_SHORT).show();
        callBacks.onEditLocation(position);

    }*/
   /* public void setCallBacks(CallBacks callBacks){
        this.callBacks=callBacks;
    }
    public interface CallBacks{
        void onEditLocation(int position);
    }*/


    private void showClientDetails(ConsumerModel consumerModel) {


    }
}

