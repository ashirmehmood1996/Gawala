package com.android.example.gawala.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Models.ConsumerModel;
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
        holder.locationTextView.setText(currentConsumer.getLongitude() + "\n " + currentConsumer.getLongitude());
        holder.timeStampTextView.setText(currentConsumer.getTime_stamp());
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
        TextView locationTextView, timeStampTextView;
        LinearLayout containerLinearLayout;


        StopsMarkersHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.tv_li_stops_markers_location);
            timeStampTextView = itemView.findViewById(R.id.tv_li_stops_markers_time_stamp);
            containerLinearLayout = itemView.findViewById(R.id.ll_li_stops_markers_container);
        }
    }

}
// TODO: 8/4/2019 LATER we will be deeling with the priorority of stops as its is a new thing to learn while side by side not necessary in the developemnt process