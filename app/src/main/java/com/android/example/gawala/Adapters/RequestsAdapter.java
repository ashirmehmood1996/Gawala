package com.android.example.gawala.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Interfaces.RequestsAdapterCallbacks;
import com.android.example.gawala.Models.RequestModel;
import com.android.example.gawala.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {
    private ArrayList<RequestModel> requestModelArrayList;
    private Context context;

    private RequestsAdapterCallbacks requestsAdapterCallbacks;

    public RequestsAdapter(ArrayList<RequestModel> requestModelArrayList, Activity activity) {
        this.requestModelArrayList = requestModelArrayList;
        this.context = activity;
        this.requestsAdapterCallbacks = (RequestsAdapterCallbacks) activity;

    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RequestViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.list_item_consumer_request, viewGroup, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, final int i) {// FIXME: 7/13/2019 later find out the drawbacks of making it final
        RequestModel currentRequestModel = requestModelArrayList.get(i);
        String name = currentRequestModel.getName();
        String lat = currentRequestModel.getLocation_lat();
        String lon = currentRequestModel.getLocation_lon();
        String timeinMilliSecStr = currentRequestModel.getTime_stamp();
        requestViewHolder.nameTextView.setText(name);


        requestViewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestsAdapterCallbacks.onRequestAccepted(i);


            }
        });
        requestViewHolder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestsAdapterCallbacks.onRequestCancel(i);

            }
        });


    }


    @Override
    public int getItemCount() {
        return requestModelArrayList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView, timestampTextView;
        Button acceptButton, rejectButton;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_li_request_name);
            locationTextView = itemView.findViewById(R.id.tv_li_request_location);
            timestampTextView = itemView.findViewById(R.id.tv_li_request_time);
            acceptButton = itemView.findViewById(R.id.bt_li_request_accept);
            rejectButton = itemView.findViewById(R.id.bt_li_request_reject);
        }
    }
}
