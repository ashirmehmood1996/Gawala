package com.android.example.gawala.Provider.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.example.gawala.Constants;
import com.android.example.gawala.Generel.Activities.ProfileActivity;
import com.android.example.gawala.Generel.AsyncTasks.GeoCoderAsyncTask;
import com.android.example.gawala.Provider.Activities.ProviderTransportersActivity;
import com.android.example.gawala.Provider.Adapters.ConnectedConsumersAdapter;
import com.android.example.gawala.Provider.Adapters.RequestsAdapter;
import com.android.example.gawala.Transporter.Interfaces.RequestsAdapterCallbacks;
import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.Provider.Models.RequestModel;
import com.android.example.gawala.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProviderClientsFragment extends DialogFragment implements RequestsAdapterCallbacks, ConnectedConsumersAdapter.Callbacks/*, ConnectedConsumersAdapter.CallBacks*/ {

    private static final String DIALOG_REQUEST_DETAILS = "dialog_request_details";
    private static final int RC_SELECT_TRANSPORTER = 1002;
    //    private static String ARG_CONSUMERS_KEY ="consumersKey";
    private RelativeLayout emptyViewContainerRelativeLayout;
    private LinearLayout newRequestscontainer, clientsContainer;
    private RecyclerView requestsRecyclerView;
    private ArrayList<RequestModel> requestModelArrayList;
    private RequestsAdapter requestsAdapter;

    private RecyclerView connectedConsumersRecyclerView;
    private ArrayList<ConsumerModel> mConsumersArrayList;
    public ConnectedConsumersAdapter consumersAdapter; //making it oubklic so that it can be updated from the activty

    private String myID;
    private ValueEventListener mClientsRequestListener;
    private ChildEventListener mClientsChildEventListener;
    private DatabaseReference mRequestNodeRef;
    private DatabaseReference mClientsRef;
    private int currentActiveRequestPosition = -1;

//    private CallBacks callBacks;

    public static ProviderClientsFragment getInstance() {

        ProviderClientsFragment fragment = new ProviderClientsFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
//        getDialog().getWindow().setWindowAnimations(R.style.dialogtheme);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//    }
////
//    @Override
//    public int getTheme() {
//        return R.style.AppTheme.NoActionBar.FullScreenDialog;
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Clients");
        View rootView = inflater.inflate(R.layout.fragment_producer_requests, container, false);
        initfields(rootView);
        attachListeners();
//        loadRequests();
        mRequestNodeRef.addValueEventListener(mClientsRequestListener);
        mClientsRef.addChildEventListener(mClientsChildEventListener);
        checkIfThereAreAnyclients();
        return rootView;
    }

    private void checkIfThereAreAnyclients() {
        rootRef.child("clients").child(myID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() && ProviderClientsFragment.this != null) {
                    clientsContainer.setVisibility(GONE);
                    if (requestModelArrayList.isEmpty()) {
                        emptyViewContainerRelativeLayout.setVisibility(VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        mRequestNodeRef.removeEventListener(mClientsRequestListener);
        mClientsRef.removeEventListener(mClientsChildEventListener);
        super.onDestroyView();
    }

    private void initfields(View rootView) {
        //dataBase related

        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRequestNodeRef = rootRef.child("requests").child(myID);
        mClientsRef = rootRef.child("clients").child(myID);

        //requests related
        emptyViewContainerRelativeLayout = rootView.findViewById(R.id.rl_pro_requests_empty_view_container);
        newRequestscontainer = rootView.findViewById(R.id.ll_pro_new_requests_container);
        clientsContainer = rootView.findViewById(R.id.ll_pro_clients_container);
        requestsRecyclerView = rootView.findViewById(R.id.rv_pro_request);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestModelArrayList = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(requestModelArrayList, getActivity(), this);
        requestsRecyclerView.setAdapter(requestsAdapter);

        //connected consumers related
        connectedConsumersRecyclerView = rootView.findViewById(R.id.rv_pro_consumers);
        connectedConsumersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mConsumersArrayList = ((TransporterMainActivity) getActivity()).getConsumersArrayList();
        mConsumersArrayList = new ArrayList<>();
        consumersAdapter = new ConnectedConsumersAdapter(mConsumersArrayList, getActivity(), this);
        connectedConsumersRecyclerView.setAdapter(consumersAdapter);


        rootView.findViewById(R.id.tv_pro_requests_back).setOnClickListener(v -> dismiss());
    }

    private void attachListeners() {

        mClientsRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestModelArrayList.clear();
                if (dataSnapshot.exists() & ProviderClientsFragment.this != null) {//then there are a number of requests
                    for (DataSnapshot requestSnap : dataSnapshot.getChildren()) {
                        String senderID = requestSnap.getKey();
                        String name = requestSnap.child("name").getValue(String.class);
                        String number = requestSnap.child("number").getValue(String.class);
                        String timeStamp = requestSnap.child("time_stamp").getValue(String.class);
                        String imageUrl = "";
                        if (requestSnap.hasChild("profile_image_uri")) {
                            imageUrl = requestSnap.child("profile_image_uri").getValue(String.class);
                        }
                        String lat = requestSnap.child("lat").getValue(String.class);
                        String lng = requestSnap.child("lng").getValue(String.class);
                        requestModelArrayList.add(new RequestModel(senderID, name, number, timeStamp, lat, lng, imageUrl));
                    }
                    newRequestscontainer.setVisibility(View.VISIBLE);
                    if (emptyViewContainerRelativeLayout.getVisibility() == VISIBLE)
                        emptyViewContainerRelativeLayout.setVisibility(GONE);
                } else {
                    newRequestscontainer.setVisibility(GONE);
                }
                requestsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mClientsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String timeStamp = dataSnapshot.child("time_stamp").getValue(String.class);

                rootRef.child("users").child(dataSnapshot.getKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (clientsContainer.getVisibility() == GONE)
                                    clientsContainer.setVisibility(View.VISIBLE);
                                if (emptyViewContainerRelativeLayout.getVisibility() == VISIBLE)
                                    emptyViewContainerRelativeLayout.setVisibility(GONE);

                                String id = userSnapshot.getKey();
                                String name = userSnapshot.child("name").getValue(String.class);
                                String number = userSnapshot.child("number").getValue(String.class);
                                String imageUri = "";
                                if (userSnapshot.hasChild("profile_image_uri")) {
                                    imageUri = userSnapshot.child("profile_image_uri").getValue(String.class);
                                }

                                String lat = userSnapshot.child("location").child("lat").getValue(String.class);
                                String lng = userSnapshot.child("location").child("lng").getValue(String.class);
                                final ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng, imageUri, 0);
                                if (lat != null) {
                                    new GeoCoderAsyncTask(getActivity()) {
                                        @Override
                                        protected void onPostExecute(Address address) {
                                            if (address != null) {
                                                consumerModel.setLocationName(address.getAddressLine(0));
                                                consumersAdapter.notifyDataSetChanged();
                                            }

                                            //call notify dataset cahnged if required
                                        }
                                    }.execute(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                                }
//                        consumerModel.setAmountOfMilk(milkDemand);

                                mConsumersArrayList.add(consumerModel);
//                                if (lat != null) {
//                                    createNewMarker(consumerModel);
//                                }


//                                countDownLatch.countDown();
//                                System.out.println("count: " + countDownLatch.getCount());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }


    @Override
    public void onRequestCancel(int position) {
        final RequestModel requestModel = requestModelArrayList.get(position);
        removeRequestNode(requestModel.getSender_id(), false, position);

    }

    @Override
    public void onRequestAccepted(final int position) {


        currentActiveRequestPosition = position;
        pickDesiredTransporter();

    }

    /**
     * this method will call upon an acivity that will let the user pick up from a list of riders to be assigned to this client for delivery
     */
    private void pickDesiredTransporter() {
        Intent intent = new Intent(getActivity(), ProviderTransportersActivity.class);
        intent.putExtra(ProviderTransportersActivity.IS_FOR_SELECTION, true);
        startActivityForResult(intent, RC_SELECT_TRANSPORTER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SELECT_TRANSPORTER) {
            if (resultCode == RESULT_OK) {
                String transporterId = data.getStringExtra(getResources().getString(R.string.transporter_id_key));
                String name = data.getStringExtra(getResources().getString(R.string.transporter_name_key));
                String number = data.getStringExtra(getResources().getString(R.string.transporter_number_key));
                acceptRequest(transporterId, name, number);
            } else {
                currentActiveRequestPosition = -1;
                Toast.makeText(getActivity(), "transporter was not selected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void acceptRequest(String id, String name, String number) {
        final RequestModel requestModel = requestModelArrayList.get(currentActiveRequestPosition);


        HashMap<String, Object> clientMap = new HashMap<>();

//        String name = requestModel.getName();
//        String number = requestModel.getNumber();
//        String time = requestModel.getTime_stamp(); //time when the request was sent
//        clientMap.put("name", name);
//        clientMap.put("number", number);
//        clientMap.put("time_stamp",time); //this time stamp is the time of sending this request
        String time_accept = Calendar.getInstance().getTimeInMillis() + "";
        clientMap.put("time_stamp", time_accept);
        clientMap.put("transporter_id", id);
        clientMap.put("transporter_name", name);
        clientMap.put("transporter_number", number);
        clientMap.put("client_id", requestModel.getSender_id());// for datbase query later

        rootRef.child("clients").child(myID)
                .child(requestModel.getSender_id())
                .setValue(clientMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addConenctedProducerNodeToDatabase(requestModel.getSender_id());
                        sendNotification(requestModel.getSender_id());
                        removeRequestNode(requestModel.getSender_id(), true, currentActiveRequestPosition);
                        //update the clients  list
//                        ((TransporterMainActivity) getActivity()).loadAllConsumers();
                    } else {
                        Toast.makeText(getContext(), "something went wrong try later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * this method is responsible to notify the respective provider that request is sent
     */
    private void sendNotification(String recieverID) {
        String message = "You are successfully connected to " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        HashMap<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", "Request Accepted");
        notificationMap.put("message", message);
        notificationMap.put("type", Constants.Notification.TYPE_REQUEST_ACCEPTED);
        notificationMap.put("sender_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        notificationMap.put("time_stamp", Calendar.getInstance().getTimeInMillis() + "");

//        for (int i = 0; i < 100; i++) {
//            String newtitle = title + "  " + i;
//            notificationMap.put("title", newtitle);
        rootRef.child("notifications")
                .child(recieverID)//reciever id
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())//sender id
                .push()//notification id
                .setValue(notificationMap);//for now no need for completion listener
//        }
    }

    @Override
    public void onRequestClientClicked(int position) {
        showRequestDetails(requestModelArrayList.get(position));
    }

    private void showRequestDetails(RequestModel requestModel) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        ClientsDetailsFragment clientsDetailsFragment = (ClientsDetailsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_REQUEST_DETAILS);
        if (clientsDetailsFragment != null) {
            fragmentTransaction.remove(clientsDetailsFragment);
        }
        ClientsDetailsFragment dialogFragment = ClientsDetailsFragment.getInstance(requestModel);
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
        rootRef.child("requests")
                .child(myID)
                .child(sender_id).removeValue().addOnCompleteListener(task -> {
            if (ProviderClientsFragment.this != null) {
                if (task.isSuccessful()) {
                    if (isAccepted) {
                        Toast.makeText(getContext(), "ClientSummeryModel added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "request removed successfully", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getContext(), "something went wrong try later", Toast.LENGTH_SHORT).show();
                }
//                requestModelArrayList.remove(position);
//                requestsAdapter.notifyItemRemoved(position);
//                requestsAdapter.notifyItemRemoved(position);
//                requestsAdapter.notifyItemRangeChanged(position, requestModelArrayList.size());
//                if (requestModelArrayList.isEmpty()) {
//                    newRequestscontainer.setVisibility(View.GONE);
//                }
            }
        });

    }

    @Override
    public void onDestroy() {
        if (mClientsRequestListener != null) {
            mRequestNodeRef.removeEventListener(mClientsRequestListener);
        }
        super.onDestroy();
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

    @Override
    public void onClientClicked(int position) {


        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.OTHER_USER, true);
        intent.putExtra(ProfileActivity.USER_ID, mConsumersArrayList.get(position).getId());
        intent.putExtra(ProfileActivity.PROVIDER_ID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        intent.putExtra(ProfileActivity.REQUEST_USER_TYPE, getResources().getString(R.string.provider));
        startActivity(intent);
    }
}

// TODO: 12/8/2019 show the loading indicator