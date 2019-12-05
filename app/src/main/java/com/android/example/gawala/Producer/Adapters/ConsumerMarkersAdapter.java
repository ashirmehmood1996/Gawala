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
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class ConsumerMarkersAdapter extends RecyclerView.Adapter<ConsumerMarkersAdapter.StopsMarkersHolder> {
    private ArrayList<ConsumerModel> mConsumerModelArrayList;
    private Context context;
    private StopMarkerClickCallBack stopMarkerClickCallBack;

    public ConsumerMarkersAdapter(ArrayList<ConsumerModel> mConsumerModelArrayList, Context context,
                                  StopMarkerClickCallBack stopMarkerClickCallBack) {
        this.mConsumerModelArrayList = mConsumerModelArrayList;
        this.context = context;
        this.stopMarkerClickCallBack = stopMarkerClickCallBack;
    }

    @NonNull
    @Override
    public StopsMarkersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StopsMarkersHolder(LayoutInflater.from(context).inflate(R.layout.li_stop_markers, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull StopsMarkersHolder holder, final int position) {
        ConsumerModel currentConsumer = mConsumerModelArrayList.get(position);
        String location = null;

        if (currentConsumer.getLocationName() == null || currentConsumer.getLocationName().isEmpty()) {
            location = currentConsumer.getLatitude() + " " + currentConsumer.getLongitude();
        } else {
            location = currentConsumer.getLocationName();
        }

        if (!currentConsumer.getImageUrl().isEmpty()) {
            Glide.with(context).load(currentConsumer.getImageUrl()).into(holder.circularImageView);
        } else {
            Glide.with(context).load(R.drawable.ic_person_black_24dp).into(holder.circularImageView);
        }
        holder.locationTextView.setText(location);
        holder.nameTextView.setText(currentConsumer.getName());

    }


    @Override
    public int getItemCount() {
        return mConsumerModelArrayList.size();
    }

    class StopsMarkersHolder extends RecyclerView.ViewHolder {
        TextView locationTextView, nameTextView;
        LinearLayout containerLinearLayout;
        CircularImageView circularImageView;

        StopsMarkersHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.tv_li_stops_markers_location);
            nameTextView = itemView.findViewById(R.id.tv_li_stops_markers_name);
            circularImageView = itemView.findViewById(R.id.civ_li_stops_markers_picture);
            containerLinearLayout = itemView.findViewById(R.id.ll_li_stops_markers_container);
            containerLinearLayout.setOnClickListener(v -> stopMarkerClickCallBack.onStopMarkerItemClick(getAdapterPosition()));
        }
    }
}
// TODO: 8/4/2019 LATER we will be deeling with the priorority of stops as its is a new thing to learn while side by side not necessary in the developemnt process