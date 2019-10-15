package com.android.example.gawala.Producer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Producer.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Producer.Models.ConsumerModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class ConsumerMarkersAdapter extends RecyclerView.Adapter<ConsumerMarkersAdapter.StopsMarkersHolder> {
    private ArrayList<ConsumerModel> mConsumerModelArrayList;
    private Context context;
    private StopMarkerClickCallBack stopMarkerClickCallBack;

    public ConsumerMarkersAdapter(ArrayList<ConsumerModel> mConsumerModelArrayList, Context context,
                                  StopMarkerClickCallBack stopMarkerClickCallBack) {
        this.mConsumerModelArrayList = mConsumerModelArrayList;
        this.context = context;
        this.stopMarkerClickCallBack=stopMarkerClickCallBack;
    }

    @NonNull
    @Override
    public StopsMarkersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StopsMarkersHolder(LayoutInflater.from(context).inflate(R.layout.li_stop_markers, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull StopsMarkersHolder holder, final int position) {
        ConsumerModel currentConsumer = mConsumerModelArrayList.get(position);
        holder.locationTextView.setText(currentConsumer.getLatitude() + "\n " + currentConsumer.getLongitude());
        holder.nameTextView.setText(currentConsumer.getName());
        holder.priorityTextView.setText(String.format("%d", position + 1));
        holder.containerLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMarkerClickCallBack.onStopMarkerItemClick(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mConsumerModelArrayList.size();
    }

    class StopsMarkersHolder extends RecyclerView.ViewHolder {
        TextView locationTextView, nameTextView,priorityTextView;
        LinearLayout containerLinearLayout;


        StopsMarkersHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.tv_li_stops_markers_location);
            nameTextView=itemView.findViewById(R.id.tv_li_stops_markers_name);
            priorityTextView=itemView.findViewById(R.id.tv_li_stops_markers_priority);
            containerLinearLayout = itemView.findViewById(R.id.ll_li_stops_markers_container);
        }
    }




}
// TODO: 8/4/2019 LATER we will be deeling with the priorority of stops as its is a new thing to learn while side by side not necessary in the developemnt process