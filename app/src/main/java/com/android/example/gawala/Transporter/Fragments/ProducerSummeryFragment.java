package com.android.example.gawala.Transporter.Fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.ClientSummeryModel;
import com.android.example.gawala.Transporter.Adapters.ProducerSummeryAdapter;
import com.android.example.gawala.Transporter.Models.ProducerSummeryModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;

public class ProducerSummeryFragment extends DialogFragment implements ProducerSummeryAdapter.CallBack {

    private static final String ARG_PROVIDER_ID = "proId";
    private static final String ARG_FROM_PROVIDER = "fromProvider";
    private Dialog mAlertDialog;
    private String myId;

    private ArrayList<ProducerSummeryModel> producerSummeryModelArrayList;
    private ProducerSummeryAdapter producerSummeryAdapter;
    private RecyclerView recyclerView;
    private String DIALOG_PRODUCER_SUMMERY_DETAILS = "SummeryDetailsDialogFagment";

    private TextView monthTextView;
    private String providerId;
    private boolean isFromProvider;

    public ProducerSummeryFragment() {
        // Required empty public constructor
    }

    public static ProducerSummeryFragment newInstance(String providerId, boolean isFromProvider) {
        ProducerSummeryFragment fragment = new ProducerSummeryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PROVIDER_ID, providerId);
        bundle.putBoolean(ARG_FROM_PROVIDER, isFromProvider);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        isFromProvider = getArguments().getBoolean(ARG_FROM_PROVIDER, false);
        providerId = getArguments().getString(ARG_PROVIDER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_producer_summery, container, false);
        initFields(rootView);
        initializeDialog();
        loadThisMonthSummary();
        return rootView;
    }

    private void initFields(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_li_prod_summery);
        producerSummeryModelArrayList = new ArrayList<>();
        producerSummeryAdapter = new ProducerSummeryAdapter(getActivity(), producerSummeryModelArrayList, this);
        recyclerView.setAdapter(producerSummeryAdapter);
        monthTextView = rootView.findViewById(R.id.tv_li_prod_summery_month);
        rootView.findViewById(R.id.ib_li_prod_summery_back).setOnClickListener(v -> dismiss());
    }

    private void loadThisMonthSummary() {
        mAlertDialog.show();
        Calendar calendar = Calendar.getInstance();//this calender object contains this month
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//if we keep day of month to 0 then the last day of previous month is also retrieved because the new day hasnot been startted yet
        //fetch this month data only
        monthTextView.setText(new SimpleDateFormat("MMMM, yyyy").format(calendar.getTimeInMillis()));
        // FIXME: 12/16/2019 show the transporter info too if this fragment is called for the provider
        rootRef.child("data")
                .child(providerId)//providerID
                .child("permanent_data")
                .orderByChild("time_stamp")
                .startAt(calendar.getTimeInMillis())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (ProducerSummeryFragment.this != null) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {

                                    String sessionId = data.getKey();
                                    long timeStamp = data.child("time_stamp").getValue(Long.class);
                                    // FIXME: 12/16/2019 bad practice we are fetching all he producer data and for no reason at all so we beed to change the data structure in firebase
                                    if (!isFromProvider) {
                                        if (data.child("transporter_id").getValue(String.class).equals(myId)) {

                                            ArrayList<ClientSummeryModel> clientSummeryModelArrayList = new ArrayList<>();

                                            for (DataSnapshot clientData : data.child("clients").getChildren()) {
//                                    float volume = clientData.child("milk_amount").getValue(Float.class);
                                                String clientId = clientData.getKey();
                                                String name = clientData.child("name").getValue(String.class);
                                                ArrayList<AcquiredGoodModel> acquiredGoodModels = new ArrayList<>();
                                                for (DataSnapshot aquiredGoodSnap : clientData.child("goods").getChildren()) {
                                                    acquiredGoodModels.add(aquiredGoodSnap.getValue(AcquiredGoodModel.class));
                                                }
                                                clientSummeryModelArrayList.add(new ClientSummeryModel(clientId, name, acquiredGoodModels));
                                            }
                                            producerSummeryModelArrayList.add(new ProducerSummeryModel(sessionId, timeStamp, clientSummeryModelArrayList));
                                        }
                                    } else {
                                        ArrayList<ClientSummeryModel> clientSummeryModelArrayList = new ArrayList<>();

                                        for (DataSnapshot clientData : data.child("clients").getChildren()) {
//                                    float volume = clientData.child("milk_amount").getValue(Float.class);
                                            String clientId = clientData.getKey();
                                            String name = clientData.child("name").getValue(String.class);
                                            ArrayList<AcquiredGoodModel> acquiredGoodModels = new ArrayList<>();
                                            for (DataSnapshot aquiredGoodSnap : clientData.child("goods").getChildren()) {
                                                acquiredGoodModels.add(aquiredGoodSnap.getValue(AcquiredGoodModel.class));
                                            }
                                            clientSummeryModelArrayList.add(new ClientSummeryModel(clientId, name, acquiredGoodModels));
                                        }
                                        producerSummeryModelArrayList.add(new ProducerSummeryModel(sessionId, timeStamp, clientSummeryModelArrayList));

                                    }
                                }
                                producerSummeryAdapter.notifyDataSetChanged();
                                mAlertDialog.dismiss();
                            } else {
                                mAlertDialog.dismiss();
                                Toast.makeText(getContext(), "there was no summary for this month", Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mAlertDialog.dismiss();

                    }
                });
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(getActivity()).setView(alertDialog).setCancelable(false).create();
    }


    @Override
    public void onRideItemclick(int position) {
        showRideDetailsDialogFragment(position);
    }

    private void showRideDetailsDialogFragment(int position) {
        ProducerSummeryModel producerSummeryModel = producerSummeryModelArrayList.get(position);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        ProducerSummeryItemDetailsFragment producerSummeryItemDetailsFragment = (ProducerSummeryItemDetailsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PRODUCER_SUMMERY_DETAILS);
        if (producerSummeryItemDetailsFragment != null) {
            fragmentTransaction.remove(producerSummeryItemDetailsFragment);
        }
        ProducerSummeryItemDetailsFragment dialogFragment = ProducerSummeryItemDetailsFragment.newInstance();
//        dialogFragment.setCallback(this);
        dialogFragment.setProducerSummeryModel(producerSummeryModel);
        dialogFragment.show(fragmentTransaction, DIALOG_PRODUCER_SUMMERY_DETAILS);
    }
}