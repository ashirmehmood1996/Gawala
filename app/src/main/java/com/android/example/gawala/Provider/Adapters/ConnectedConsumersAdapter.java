package com.android.example.gawala.Provider.Adapters;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class ConnectedConsumersAdapter extends RecyclerView.Adapter<ConnectedConsumersAdapter.ConnectedConsumersHolder> {
    private ArrayList<ConsumerModel> consumerModelArrayList;
    private Context context;
    private Callbacks callbacks;
//    private CallBacks callBacks;

    public ConnectedConsumersAdapter(ArrayList<ConsumerModel> consumerModelArrayList, Activity activity, Callbacks callBacks) {
        this.consumerModelArrayList = consumerModelArrayList;
        this.context = activity;
        this.callbacks = callBacks;

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

        if (!consumerModel.getImageUrl().isEmpty()) {
            Glide.with(context).load(consumerModel.getImageUrl()).into(holder.circularImageView);
        } else {
            holder.circularImageView.setBackgroundResource(R.drawable.ic_person_black_24dp);
        }
        if (consumerModel.getLatitude() != null || consumerModel.getLongitude() != null) {
            String location = null;

            if (consumerModel.getLocationName() == null || consumerModel.getLocationName().isEmpty()) {
                location = "lat : " + consumerModel.getLatitude() + "\n" +
                        "lon : " + consumerModel.getLongitude();
            } else {
                location = consumerModel.getLocationName();
            }
            holder.locationTextView.setText(location);
        } else {
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
        CircularImageView circularImageView;
        LinearLayout containerLinearLayout;
//        Button editLocationButton;

        public ConnectedConsumersHolder(@NonNull View itemView) {
            super(itemView);
            circularImageView = itemView.findViewById(R.id.civ_li_conected_con_picture);
            nameTextView = itemView.findViewById(R.id.tv_li_conected_con_name);
            locationTextView = itemView.findViewById(R.id.tv_li_conected_con_location);
            containerLinearLayout = itemView.findViewById(R.id.ll_li_conected_con_container);
            containerLinearLayout.setOnClickListener(v -> callbacks.onClientClicked(getAdapterPosition()));

//            editLocationButton =itemView.findViewById(R.id.bt_li_conected_con_edit_location);
        }
    }


    /* */

    /**
     * an interfase between activity and adapter
     *//*
//    public interface CallBacks{
//        void onEditLocation(int position);
//    }*/
    public interface Callbacks {
        void onClientClicked(int position);
    }
}
