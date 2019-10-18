package com.android.example.gawala.Producer.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.Producer.Activities.ProducerNavMapActivity;
import com.android.example.gawala.R;

import java.util.ArrayList;


public class AciveRideStopsAdaper extends RecyclerView.Adapter<AciveRideStopsAdaper.ActiverideViewHolder> {
    private Context context;
    private ArrayList<ConsumerModel> consumerModelArrayList;
//    private Callbacks callbacks;

    /**
     *
     * @param context
     * @param consumerModelArrayList the llist of consumers to which goods are needed to be sent
     */
    public AciveRideStopsAdaper(Context context, ArrayList<ConsumerModel> consumerModelArrayList/*, Callbacks callbacks*/) {
        this.context = context;
        this.consumerModelArrayList = consumerModelArrayList;
//        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public ActiverideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActiverideViewHolder(LayoutInflater.from(context).inflate(R.layout.li_active_ride_stops, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ActiverideViewHolder holder, int position) {
        ConsumerModel consumerModel = consumerModelArrayList.get(position);
        float milkAmount = consumerModel.getAmountOfMilk();
        String name = consumerModel.getName();
        String location;

        if (consumerModel.getLocationName()==null || consumerModel.getLocationName().isEmpty()){
            location = consumerModel.getLatitude() + " " + consumerModel.getLongitude();
        }else {
            location = consumerModel.getLocationName();
        }



        holder.priorityTextView.setText((position+1)+"");

        holder.nameTextView.setText(name);
        holder.locationTextView.setText(location);

        boolean status = consumerModel.isDelivered();
        if (ProducerNavMapActivity.activeStopPosition != position) {
            if (status) {
                holder.statusTextView.setText(String.format("%.1f litre(s)\nDelivered",milkAmount));
                holder.statusTextView.setTextColor(Color.BLUE);
            } else {
                holder.statusTextView.setText(String.format("%.1f litre(s)\nPending",milkAmount));
                holder.statusTextView.setTextColor(Color.GRAY);
            }
        }else {
            holder.statusTextView.setText(String.format("%.1f litre(s)\nCarrying",milkAmount));
            holder.statusTextView.setTextColor(Color.GREEN);
        }

    }

    @Override
    public int getItemCount() {
        return consumerModelArrayList.size();
    }

    class ActiverideViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerLinearLayout;
        TextView nameTextView, locationTextView, statusTextView,priorityTextView;


        ActiverideViewHolder(@NonNull View itemView) {
            super(itemView);

            containerLinearLayout = itemView.findViewById(R.id.ll_li_active_ride_container);
            nameTextView = itemView.findViewById(R.id.tv_li_active_ride_client_name);
            locationTextView = itemView.findViewById(R.id.tv_li_active_ride_client_address);
            statusTextView = itemView.findViewById(R.id.tv_li_active_ride_client_status);
            priorityTextView=itemView.findViewById(R.id.tv_li_active_ride_priority);
            atachListeners();
        }

        private void atachListeners() {
            containerLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, String.format("%d position clicked", getAdapterPosition()), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
//
//    public class Callbacks {
//
//    }
}
