package com.android.example.gawala.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.example.gawala.Models.ConnectedConsumersModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class ConnectedConsumersAdapter extends RecyclerView.Adapter<ConnectedConsumersAdapter.ConnectedConsumersHolder> {
    private ArrayList<ConnectedConsumersModel> connectedConsumersModelArrayList;
    private Context context;

    public ConnectedConsumersAdapter(ArrayList<ConnectedConsumersModel> connectedConsumersModelArrayList, Context context) {
        this.connectedConsumersModelArrayList = connectedConsumersModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ConnectedConsumersHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ConnectedConsumersHolder(LayoutInflater.from(context)
                .inflate(R.layout.list_item_consumers_connected, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedConsumersHolder connectedConsumersHolder, int i) {
        ConnectedConsumersModel connectedConsumersModel = connectedConsumersModelArrayList.get(i);
        connectedConsumersHolder.nameTextView.setText(connectedConsumersModel.getName());
        connectedConsumersHolder.locationTextView.setText(connectedConsumersModel.getNumber());


    }

    @Override
    public int getItemCount() {
        return connectedConsumersModelArrayList.size();
    }

    class ConnectedConsumersHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView;

        public ConnectedConsumersHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_li_conected_con_name);
            locationTextView = itemView.findViewById(R.id.tv_li_conected_con_location);
        }
    }
}
