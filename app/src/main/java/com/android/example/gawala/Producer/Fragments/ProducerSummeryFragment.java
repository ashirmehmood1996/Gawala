package com.android.example.gawala.Producer.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Producer.Adapters.ProducerSummeryAdapter;
import com.android.example.gawala.Generel.Models.ClientSummery;
import com.android.example.gawala.Producer.Models.ProducerSummeryModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ProducerSummeryFragment extends Fragment {

    private Dialog mAlertDialog;
    private String myId;

    private ArrayList<ProducerSummeryModel> producerSummeryModelArrayList;
    private ProducerSummeryAdapter producerSummeryAdapter;
    private RecyclerView recyclerView;

    public ProducerSummeryFragment() {
        // Required empty public constructor
    }

    public static ProducerSummeryFragment newInstance() {
        ProducerSummeryFragment fragment = new ProducerSummeryFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Rides Summery");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
        producerSummeryAdapter = new ProducerSummeryAdapter(getActivity(), producerSummeryModelArrayList);
        recyclerView.setAdapter(producerSummeryAdapter);

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
        FirebaseDatabase.getInstance().getReference().child("data")
                .child(myId)
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

                                    ArrayList<ClientSummery> clientSummeryArrayList = new ArrayList<>();
                                    for (DataSnapshot clientData : data.child("clients").getChildren()) {
//                                    float volume = clientData.child("milk_amount").getValue(Float.class);
                                        String clientId = clientData.getKey();
                                        String name = clientData.child("name").getValue(String.class);
                                        ArrayList<AcquiredGoodModel> acquiredGoodModels = new ArrayList<>();
                                        for (DataSnapshot aquiredGoodSnap : clientData.child("goods").getChildren()) {
                                            acquiredGoodModels.add(aquiredGoodSnap.getValue(AcquiredGoodModel.class));
                                        }


                                        clientSummeryArrayList.add(new ClientSummery(clientId, name, acquiredGoodModels));
                                    }
                                    producerSummeryModelArrayList.add(new ProducerSummeryModel(sessionId, timeStamp, clientSummeryArrayList));
                                }

                                producerSummeryAdapter.notifyDataSetChanged();
                                mAlertDialog.dismiss();
                            } else {
                                mAlertDialog.dismiss();
                                Toast.makeText(getContext(), "there was no summary for this month", Toast.LENGTH_LONG).show();
                                getActivity().getSupportFragmentManager().beginTransaction().remove(ProducerSummeryFragment.this).commit();
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
    public void onDetach() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Map");

        super.onDetach();
    }

    private String getFormattedData(long timeInMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM yyyy ");
        return simpleDateFormat.format(timeInMilliseconds);
    }
}

