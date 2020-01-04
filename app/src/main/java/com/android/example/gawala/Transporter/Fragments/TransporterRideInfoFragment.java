package com.android.example.gawala.Transporter.Fragments;

import android.app.AlertDialog;
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
import android.widget.LinearLayout;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Transporter.Adapters.ConsumerMarkersAdapter;
import com.android.example.gawala.Transporter.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;


import java.util.ArrayList;
import java.util.HashMap;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransporterRideInfoFragment extends DialogFragment implements StopMarkerClickCallBack {
    private static final String ARG_MARKERS_KEY = "markersKey";
    private static final String TAG = "RideInfoFragment";
    private static final String ARG_PROVIDER_ID = "providerId";

    private String myId;
    private Query mClientsRef;
    private String providerId;

    private Callbacks callbacks;

    private ArrayList<ConsumerModel> mActiveRidesArrayList;
    private ConsumerMarkersAdapter mConsumerMarkersAdapter;
    private RecyclerView mRecyclerView;


    private Button startRideButton;
    private ImageButton backButton;

    private ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList;
    private HashMap<String, Object> goodsToCarryHashMap;
    //    private ArrayList<ConsumerModel> mConsumerModelArrayList;
    private AlertDialog mProgressDialog;

    public TransporterRideInfoFragment() {
        // Required empty public constructor
    }

    /**
     * @param aactiveRidesArrayList the list of clients taht are actively participating in the current ride
     * @return
     */

    public static TransporterRideInfoFragment newInstance(ArrayList<ConsumerModel> aactiveRidesArrayList, String providerId) {


        ArrayList<ConsumerModel> activeRidesArrayList = new ArrayList<>();
        for (ConsumerModel consumerModel : aactiveRidesArrayList) {
            activeRidesArrayList.add(consumerModel.getConsumerModel());
        }

        TransporterRideInfoFragment fragment = new TransporterRideInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MARKERS_KEY, activeRidesArrayList);
        args.putString(ARG_PROVIDER_ID, providerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);

//        if (getArguments() != null) {
//            providerId = getArguments().getString(ARG_PROVIDER_ID, null);
//        }

        if (getArguments() != null) {
            mActiveRidesArrayList = (ArrayList<ConsumerModel>) getArguments().getSerializable(ARG_MARKERS_KEY);
            providerId = getArguments().getString(ARG_PROVIDER_ID, null);


            acquiredGoodModelArrayList = new ArrayList<>();

            for (ConsumerModel consumerModel : mActiveRidesArrayList) {
//                consumerModel.setMarker(null);// making it null to avoid serialization issue
//                consumerModel.getDemandArray();
//                String itemName = "";
                acquiredGoodModelArrayList.addAll(consumerModel.getDemandArray());
//                LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.li_good_to_carry, null);
//                TextView nameTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_name);
//                TextView totalUnitsTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_units);

            }
//            for (AcquiredGoodModel acquiredGoodModel : acquiredGoodModelArrayList) {
//                String key = acquiredGoodModel.getGoodModel().getName();
//
//                if (goodsToCarryHashMap.containsKey(key)) {
//                    HashMap<String, Object> subMap = (HashMap<String, Object>) goodsToCarryHashMap.get(key);
//                    Integer previousDemand = (Integer) subMap.get("demand");
//                    Integer newDemand = Integer.parseInt(acquiredGoodModel.getDemand());
//                    Integer totalDemand = previousDemand + newDemand;
//                    subMap.put("demand", totalDemand);
//                    goodsToCarryHashMap.put(key, subMap);
//
//                } else {
//                    HashMap<String, Object> subMap = new HashMap<>();
//                    subMap.put("name", acquiredGoodModel.getGoodModel().getName());
//                    subMap.put("image", acquiredGoodModel.getGoodModel().getImage_uri());
//                    subMap.put("demand", Integer.parseInt(acquiredGoodModel.getDemand()));
//                    goodsToCarryHashMap.put(key, subMap);
//                }
//            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_transporter_ride_info, container, false);
        initFields(rootView);
        attachListeners();
//        loadAllConsumers();

//        captureMilkDemandToday();
//        showDemandList();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void initFields(View rootView) {
        initializeDialog();
        goodsToCarryHashMap = new HashMap<>();

//        mConsumerModelArrayList = new ArrayList<>();
//        mActiveRidesArrayList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.rv_frag_prod_dash_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mConsumerMarkersAdapter = new ConsumerMarkersAdapter(mActiveRidesArrayList, getActivity(), this);
        mRecyclerView.setAdapter(mConsumerMarkersAdapter);
        startRideButton = rootView.findViewById(R.id.bt_frag_prod_dash_board_start_ride);
        backButton = rootView.findViewById(R.id.iv_frag_prod_dash_board_back);

        //data related
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mClientsRef = rootRef.child("clients").child(providerId)//this will only work for a single provider but if we provide for multiple providers then we may change the query
                .orderByChild("transporter_id").equalTo(myId);


    }

    private void attachListeners() {

        backButton.setOnClickListener(v -> dismiss());
        startRideButton.setOnClickListener(v ->
        {
            for (AcquiredGoodModel acquiredGoodModel : acquiredGoodModelArrayList) {
                String key = acquiredGoodModel.getGoodModel().getName();

                if (goodsToCarryHashMap.containsKey(key)) {
                    HashMap<String, Object> subMap = (HashMap<String, Object>) goodsToCarryHashMap.get(key);
                    Integer previousDemand = (Integer) subMap.get("demand");
                    Integer newDemand = Integer.parseInt(acquiredGoodModel.getDemand());
                    Integer totalDemand = previousDemand + newDemand;
                    subMap.put("demand", totalDemand);
                    goodsToCarryHashMap.put(key, subMap);

                } else {
                    HashMap<String, Object> subMap = new HashMap<>();
                    subMap.put("name", acquiredGoodModel.getGoodModel().getName());
                    subMap.put("image", acquiredGoodModel.getGoodModel().getImage_uri());
                    subMap.put("demand", Integer.parseInt(acquiredGoodModel.getDemand()));
                    goodsToCarryHashMap.put(key, subMap);
                }
            }
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

//
//    public void loadAllConsumers() {
//        mConsumerModelArrayList.clear();
//        mProgressDialog.show();
//        rootRef.child("clients").child(providerId)//prodcuer id
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists() && TransporterRideInfoFragment.this != null) {
//
//                            new AsyncTask<Void, Void, Boolean>() {
//                                @Override
//                                protected Boolean doInBackground(Void... voids) {
//
//                                    CountDownLatch countDownLatch = new CountDownLatch((int) dataSnapshot.getChildrenCount());
//                                    for (DataSnapshot clientSnap : dataSnapshot.getChildren()) {
//
//                                        String timeStamp = clientSnap.child("time_stamp").getValue(String.class);
//
//                                        rootRef.child("users").child(clientSnap.getKey())
//                                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
//
//                                                        String id = userSnapshot.getKey();
//                                                        String name = userSnapshot.child("name").getValue(String.class);
//                                                        String number = userSnapshot.child("number").getValue(String.class);
//                                                        String imageUri = "";
//                                                        if (userSnapshot.hasChild("profile_image_uri")) {
//                                                            imageUri = userSnapshot.child("profile_image_uri").getValue(String.class);
//                                                        }
//
//                                                        String lat = userSnapshot.child("location").child("lat").getValue(String.class);
//                                                        String lng = userSnapshot.child("location").child("lng").getValue(String.class);
//
//                                                        final ConsumerModel consumerModel = new ConsumerModel(id, name, number, timeStamp, lat, lng, imageUri);
//                                                        if (lat != null && getActivity() != null) {
//                                                            new GeoCoderAsyncTask(getActivity().getApplicationContext()) {
//                                                                @Override
//                                                                protected void onPostExecute(Address address) {
//                                                                    if (address != null) {
//                                                                        consumerModel.setLocationName(address.getAddressLine(0));
//                                                                    }
//                                                                    //call notify dataset cahnged if required
//                                                                }
//                                                            }.execute(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
//                                                        }
////                        consumerModel.setAmountOfMilk(milkDemand);
//
//                                                        mConsumerModelArrayList.add(consumerModel);
////                                                        if (lat != null) {
////                                                            createNewMarker(consumerModel);
////                                                        }
//
//
//                                                        countDownLatch.countDown();
//                                                        System.out.println("count: " + countDownLatch.getCount());
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                                    }
//                                                });
//
//
//                                    }
//                                    try {
//                                        countDownLatch.await();
//                                        return true;
//
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                        return false;
//                                    }
//                                }
//
//                                @Override
//                                protected void onPostExecute(Boolean aBoolean) {
//                                    if (getContext() != null) {
//                                        if (aBoolean) {
//                                            if (mProgressDialog.isShowing()) {
//                                                mProgressDialog.dismiss();
//                                                mProgressDialog.show();
//                                            }
//                                            checkIfConsumerIsOnVacation();
////                                        Toast.makeText(getActivity(), "all data is fetched", Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            mProgressDialog.dismiss();
//                                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
//                                        }
//
////                                    if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) != null) {
////                                        ProviderClientsFragment providerClientsFragment = (ProviderClientsFragment) mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG);
////                                        if (providerClientsFragment.consumersAdapter != null) {
////                                            providerClientsFragment.consumersAdapter.notifyDataSetChanged();
////                                        }
////                                    }
//                                    }
//                                }
//                            }.execute();
//
//
//                        } else {
////                            if (mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG) != null) {
////                                ProviderClientsFragment providerClientsFragment = (ProviderClientsFragment) mFragmentManager.findFragmentByTag(PRODUCER_CLIENT_REQUEST_FRAGMENT_TAG);
////                                if (providerClientsFragment.consumersAdapter != null) {
////                                    providerClientsFragment.consumersAdapter.notifyDataSetChanged();
////                                }
////                            }
//                            mProgressDialog.dismiss();
//                            Toast.makeText(getActivity(), "no clients were added", Toast.LENGTH_SHORT).show();
//
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        mProgressDialog.dismiss();
//                        Toast.makeText(getActivity(), "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//// TODO: 11/22/2019 update the data when child changes his her location or may be the client should not because this will require the provider to rethink that of he/she wants to deliver to that new location or not
//
////        rootRef.child("clients").child(myID)//prodcuer id
////                .addChildEventListener(new ChildEventListener() {
////                    @Override
////                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
////
////                    }
////
////                    @Override
////                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
////
////                        //it is assumed that this method will only be activated when the location for a consumer marker is updated
////                        String id = dataSnapshot.getKey();
////
////                        String lat = null;
////                        String lng = null;
////                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
////                            lat = dataSnapshot.child("lat").getValue(String.class);
////                            lng = dataSnapshot.child("lng").getValue(String.class);
////                        }
////                        ConsumerModel currentConsumerModel = null;
////                        for (final ConsumerModel currentModel : mConsumerModelArrayList) {
////                            if (currentModel.getId().equals(id)) {
////                                currentConsumerModel = currentModel;
////                                currentConsumerModel.setLat(lat);
////                                currentConsumerModel.setLng(lng);
////
////
////                                if (lat != null) {
////                                    new GeoCoderAsyncTask(TransporterMainActivity.this) {
////                                        @Override
////                                        protected void onPostExecute(Address address) {
////                                            currentModel.setLocationName(address.getAddressLine(0));
////                                            //call notify dataset cahnged if required
////                                        }
////                                    }.execute(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
////                                }
////                                //update the location string later
////
////                                break;
////                            }
////                        }
////                        if (currentConsumerModel == null) return;
////
////                        if (lat != null && currentConsumerModel.getMarker() == null) {//if the marker is null then the location is added for the first time and a new marker is needed
////                            createNewMarker(currentConsumerModel);
////                        }
////                    }
////
////                    @Override
////                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
////
////                    }
////
////                    @Override
////                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
////
////                    }
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////
////                    }
////                });
//
//    }


//    private void checkIfConsumerIsOnVacation() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MILLISECOND, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... voids) {
//                CountDownLatch countDownLatch = new CountDownLatch(mConsumerModelArrayList.size());//
//
////        int i = 0;
//                for (ConsumerModel consumerModel : mConsumerModelArrayList) {
//
////            int finalI = i;
//                    rootRef.child("days_off").child(consumerModel.getId())
//                            .child("days").child(calendar.getTimeInMillis() + "").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists()) {
//                                Log.d(TAG, "doInBackground: Someone is on vacation");
//                                consumerModel.setOnVacation(true);
//                            }
//
////                    if (finalI == mConsumerModelArrayList.size() - 1) {//then it is final result
////                        fetchFreshDataOfUsers();
////                    }
//                            countDownLatch.countDown();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
////            i++;
//                }
//
//
//                try {
//                    countDownLatch.await();
//                    Log.d(TAG, "doInBackground: wait is called");
//                    return true;
//                } catch (InterruptedException e) {
//                    Log.d(TAG, "doInBackground: exception is thrown");
//
//                    e.printStackTrace();
//                    return false;
//                }
//
//            }
//
//            @Override
//            protected void onPostExecute(Boolean success) {
//                if (success && getActivity() != null) {
//
//                    fetchFreshy();
////                    fetchFreshDataOfUsers();
//                } else {
//                    //await was not done and exception was thrown
//                }
//
//            }
//        }.execute();
//
//    }
//
//
//    private void fetchFreshy() {
//        mProgressDialog.show();
//        mActiveRidesArrayList.clear();
//        fetchFreshyfor(0);
//
//    }
//
//    /**
//     * @param position postion of one of the child from connected consumers
//     */
//    private void fetchFreshyfor(int position) {
//        if (mConsumerModelArrayList.isEmpty()) { //if there are no consumers
//            Toast.makeText(getActivity(), "you donot have any consumers connected", Toast.LENGTH_SHORT).show();
//            mProgressDialog.dismiss();
//            return;
//        }
//
//        if (position >= mConsumerModelArrayList.size()) { // if this is the beyond last consumer
//            mConsumerMarkersAdapter.notifyDataSetChanged();
//            mProgressDialog.dismiss();
//            return;
//        }
////        if (mConsumerModelArrayList.size() == position + 1) { // if this is the last consumer
////            startRideInfoFragment();
////            return;
////        }
//        ConsumerModel consumerModel = mConsumerModelArrayList.get(position);
//
//        if (consumerModel.isOnVacation()) {
//            fetchFreshyfor(position + 1);
//        } else {
//            //fetch demand data for this consumer
//
//
//            rootRef.child("demand")
//                    .child(providerId)//producer id
//                    .child(consumerModel.getId())//consumer id
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                            if (dataSnapshot.exists() && this != null) {
//
//                                new AsyncTask<Void, Void, Boolean>() {
//                                    @Override
//                                    protected Boolean doInBackground(Void... voids) {
//
//
//                                        ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
//                                        consumerModel.setDemandArray(demandArray);
//                                        mActiveRidesArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
//
//                                        CountDownLatch countDownLatch = new CountDownLatch((int) dataSnapshot.getChildrenCount());
//
//                                        for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
//
//
//                                            String goodID = goodSnap.getKey();
//                                            String demandUnits = goodSnap.child("demand").getValue(String.class);
//                                            if (demandUnits.equals("0")) { // TODO: 10/14/2019 test this
//                                                countDownLatch.countDown();
//                                                continue;
//                                            } else {
//                                                consumerModel.setHasDemand(true);
//                                                rootRef.child("goods").child(providerId).child(goodID)
//                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                                                            @Override
//                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                                if (dataSnapshot.exists() && this != null) {
//                                                                    if (dataSnapshot.exists()) {
//                                                                        GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
//                                                                        demandArray.add(new AcquiredGoodModel(demandUnits, providerId, goodModel));
////                                                                       demandArray.notifyDataSetChanged();
//                                                                    } else {
////                                                                       Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
//                                                                    }
//                                                                }
//                                                                countDownLatch.countDown();
//                                                            }
//
//                                                            @Override
//                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                                countDownLatch.countDown();
//                                                                Toast.makeText(getActivity(), "datbase error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                                                            }
//                                                        });
//                                            }
//                                        }
//
//                                        try {
//                                            countDownLatch.await();
//                                            Log.d(TAG, "doInBackground: wait is called");
//                                            return true;
//                                        } catch (InterruptedException e) {
//                                            Log.d(TAG, "doInBackground: exception is thrown");
//
//                                            e.printStackTrace();
//                                            return false;
//                                        }
//                                    }
//
//                                    @Override
//                                    protected void onPostExecute(Boolean aBoolean) {
//
//
//                                        fetchFreshyfor(position + 1);
//                                    }
//                                }.execute();
//
////                                ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
////                                consumerModel.setDemandArray(demandArray);
////                                mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
////                                int j = 1;
////
////                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
////
////                                    String good_id = goodSnap.getKey();
////                                    String demandUnits = goodSnap.child("demand").getValue(String.class);
////                                    if (demandUnits.equals("0")) { // TODO: 10/14/2019 test this
////                                        continue;
////                                    } else {
////                                        consumerModel.setHasDemand(true);
////                                    }
////                                    if ((mConsumerModelArrayList.size() == finalI + 1)
////                                            && j == dataSnapshot.getChildrenCount()) {// if its last outer adn inner array list call then the progress dialog shoud disappear
////                                        isFinalCall[0] = true;
////                                    }
////                                    fetchGoodDetailFromFireabse(good_id, demandUnits, demandArray, isFinalCall[0]);
////
////                                    j++;
////                                }
////                                if (!consumerModel.hasDemand()) {
////                                    mActiveRideArrayList.remove(consumerModel);
////                                    if ((mConsumerModelArrayList.size() == finalI + 1)) {
////                                        startRideInfoFragment();
////                                        mProgressDialog.dismiss();
////                                        Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
////                                    }
////                                }
//////                                acquiredGoodsAdapter.notifyDataSetChanged();
////                            } else {
////                                if ((mConsumerModelArrayList.size() == finalI + 1)) {
////                                    startRideInfoFragment();
////                                    mProgressDialog.dismiss();
////                                    Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
////                                }
////
////                            }
////
////                        }
////
////                        @Override
////                        public void onCancelled(@NonNull DatabaseError databaseError) {
////
//                            } else {
//                                fetchFreshyfor(position + 1);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//        }
//    }
//
////
////    private void fetchFreshDataOfUsers() {
////
////        final boolean[] isFinalCall = {false};//to make sure that all call has been made while assuming the task is done sequeciallt whihc it is not
////        mActiveRideArrayList.clear();
////
////
////        for (int i = 0; i < mConsumerModelArrayList.size(); i++) {
////            final ConsumerModel consumerModel = mConsumerModelArrayList.get(i);//this loop will get demand for each consumer
////            if (consumerModel.isOnVacation()) {//if this consumer is on vacation then no need to fetch dat for this consumer
////                if (i == mConsumerModelArrayList.size() - 1) {
////                    startRideInfoFragment();
////                    mProgressDialog.dismiss();
////                }
////                continue;
////            }
////            final int finalI = i;
////            rootRef.child("demand")
////                    .child(myID)//producer id
////                    .child(consumerModel.getId())//consumer id
////                    .addListenerForSingleValueEvent(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
////                                ArrayList<AcquiredGoodModel> demandArray = new ArrayList<>();
////                                consumerModel.setDemandArray(demandArray);
////                                mActiveRideArrayList.add(consumerModel); //this is making the 0 values for consumers that are not delivered any stuff
////                                int j = 1;
////
////                                for (DataSnapshot goodSnap : dataSnapshot.getChildren()) {
////                                    String good_id = goodSnap.getKey();
////                                    String demandUnits = goodSnap.child("demand").getValue(String.class);
////                                    if (demandUnits.equals("0")) { // TODO: 10/14/2019 test this
////                                        continue;
////                                    } else {
////                                        consumerModel.setHasDemand(true);
////                                    }
////                                    if ((mConsumerModelArrayList.size() == finalI + 1)
////                                            && j == dataSnapshot.getChildrenCount()) {// if its last outer adn inner array list call then the progress dialog shoud disappear
////                                        isFinalCall[0] = true;
////                                    }
////                                    fetchGoodDetailFromFireabse(good_id, demandUnits, demandArray, isFinalCall[0]);
////
////                                    j++;
////                                }
////                                if (!consumerModel.hasDemand()) {
////                                    mActiveRideArrayList.remove(consumerModel);
////                                    if ((mConsumerModelArrayList.size() == finalI + 1)) {
////                                        startRideInfoFragment();
////                                        mProgressDialog.dismiss();
////                                        Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
////                                    }
////                                }
//////                                acquiredGoodsAdapter.notifyDataSetChanged();
////                            } else {
////                                if ((mConsumerModelArrayList.size() == finalI + 1)) {
////                                    startRideInfoFragment();
////                                    mProgressDialog.dismiss();
////                                    Toast.makeText(TransporterMainActivity.this, "All data is fetched", Toast.LENGTH_SHORT).show();
////                                }
////
////                            }
////
////                        }
////
////                        @Override
////                        public void onCancelled(@NonNull DatabaseError databaseError) {
//////                            Toast.makeText(AcquiredGoodsActivity.this, String.format("fetching Services step for %s was cancelled due to error:%s", producerID, databaseError.getMessage()), Toast.LENGTH_SHORT).show();
////                            mProgressDialog.dismiss();
////
////                        }
////                    });
////        }
////    }
//
//
////    private void fetchGoodDetailFromFireabse(String goodID, final String demand, final ArrayList<AcquiredGoodModel> demandArray, final boolean isFinalCall) {
////
////        // TODO: 10/14/2019 make some indicator that the data is being fetched
////        rootRef
////                .child("goods").child(myID).child(goodID)
////                .addListenerForSingleValueEvent(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                        if (dataSnapshot.exists() && TransporterMainActivity.this != null) {
////                            if (dataSnapshot.exists()) {
////                                GoodModel goodModel = dataSnapshot.getValue(GoodModel.class);
////                                demandArray.add(new AcquiredGoodModel(demand, myID, goodModel));
////
////
//////                            demandArray.notifyDataSetChanged();
////                            } else {
//////                            Toast.makeText(AcquiredGoodsActivity.this, "couldn't find this good", Toast.LENGTH_SHORT).show();
////                            }
////
////                            if (isFinalCall) {
////                                startRideInfoFragment();
////                                mProgressDialog.dismiss();
////                                Toast.makeText(getApplicationContext(), "All data is fetched", Toast.LENGTH_SHORT).show();
////                            }
////                        }
////                    }
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////                        Toast.makeText(getApplicationContext(), "datbase error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
////                    }
////                });
////
////    }


    public interface Callbacks {
        void onStopMarkerItemClick(int position);

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

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mProgressDialog = new AlertDialog.Builder(getContext()).setView(alertDialog).setCancelable(false).create();
    }
}


//    show the each type of items akong with respective amount that is needed to
//    be carried our for the journy and then move to next task all data is there
//    in array list just need to organize a little the goods type can be differencited
//    on the bases of ids so we need just to make the logic
