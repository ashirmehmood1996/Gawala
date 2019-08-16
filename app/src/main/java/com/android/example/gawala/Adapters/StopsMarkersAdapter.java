package com.android.example.gawala.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Interfaces.StopMarkerClickCallBack;
import com.android.example.gawala.Models.StopMarkerModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class StopsMarkersAdapter extends RecyclerView.Adapter<StopsMarkersAdapter.StopsMarkersHolder> {
    private ArrayList<StopMarkerModel> mStopMarkerModelArrayList;
    private Context context;
    private StopMarkerClickCallBack stopMarkerClickCallBack;

    public StopsMarkersAdapter(ArrayList<StopMarkerModel> mStopMarkerModelArrayList, Context context,
                               StopMarkerClickCallBack stopMarkerClickCallBack) {
        this.mStopMarkerModelArrayList = mStopMarkerModelArrayList;
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
        StopMarkerModel currentStop = mStopMarkerModelArrayList.get(position);
        holder.locationTextView.setText(currentStop.getLongitude() + " " + currentStop.getLongitude());
        holder.timeStampTextView.setText(currentStop.getTimeStamp() + "");
        holder.containerLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMarkerClickCallBack.onStopMarkerItemClick(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mStopMarkerModelArrayList.size();
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