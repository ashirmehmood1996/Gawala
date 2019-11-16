package com.android.example.gawala.Producer.Adapters;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class ConnectedConsumersAdapter extends RecyclerView.Adapter<ConnectedConsumersAdapter.ConnectedConsumersHolder> {
    private ArrayList<ConsumerModel> consumerModelArrayList;
    private Context context;
//    private CallBacks callBacks;

    public ConnectedConsumersAdapter(ArrayList<ConsumerModel> consumerModelArrayList, Activity activity/*, CallBacks callBacks*/) {
        this.consumerModelArrayList = consumerModelArrayList;
        this.context = activity;
//        this.callBacks= callBacks;

    }

    @NonNull
    @Override
    public ConnectedConsumersHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ConnectedConsumersHolder(LayoutInflater.from(context)
                .inflate(R.layout.li_consumers_connected, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedConsumersHolder holder, final int i) {
        ConsumerModel consumerModel = consumerModelArrayList.get(i);
        holder.nameTextView.setText(consumerModel.getName());




        if(consumerModel.getLatitude()!=null || consumerModel.getLongitude()!=null){
            String location=null;

            if (consumerModel.getLocationName()==null || consumerModel.getLocationName().isEmpty()){
                location = "lat : "+ consumerModel.getLatitude()+"\n" +
                        "lon : "+ consumerModel.getLongitude();
            }else {
                location = consumerModel.getLocationName();
            }
            holder.locationTextView.setText(location);
        }else {
            holder.locationTextView.setText("location was not set");
        }
/*
        holder.editLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBacks.onEditLocation(i);
            }
        });*/


    }

    @Override
    public int getItemCount() {
        return consumerModelArrayList.size();
    }

    class ConnectedConsumersHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView;
//        Button editLocationButton;

        public ConnectedConsumersHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_li_conected_con_name);
            locationTextView = itemView.findViewById(R.id.tv_li_conected_con_location);
//            editLocationButton =itemView.findViewById(R.id.bt_li_conected_con_edit_location);
        }
    }


   /* *//**
     * an interfase between activity and adapter
     *//*
    public interface CallBacks{
        void onEditLocation(int position);
    }*/
}
