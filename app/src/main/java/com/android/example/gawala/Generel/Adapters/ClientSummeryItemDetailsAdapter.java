package com.android.example.gawala.Generel.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class ClientSummeryItemDetailsAdapter extends RecyclerView.Adapter<ClientSummeryItemDetailsAdapter.SummeryDetailViewHolder> {
    private ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList;
    private Context context;

    public ClientSummeryItemDetailsAdapter(ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList, Context context) {

        this.acquiredGoodModelArrayList = acquiredGoodModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SummeryDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new SummeryDetailViewHolder(LayoutInflater.from(context).inflate(R.layout.li_dialog_client_summery_item_details, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull SummeryDetailViewHolder holder, int position) {

        AcquiredGoodModel acquiredGoodModel = acquiredGoodModelArrayList.get(position);
        String name = acquiredGoodModel.getGoodModel().getName();
        String price = acquiredGoodModel.getGoodModel().getPrice();
        String quantity = acquiredGoodModel.getDemand();
        String netAmount = "" + (Integer.parseInt(price) * Integer.parseInt(quantity));
        holder.nameTextView.setText(name);
        holder.priceTextView.setText(price);
        holder.quantityTextView.setText(quantity);
        holder.netTextView.setText(netAmount);

    }

    @Override
    public int getItemCount() {
        return acquiredGoodModelArrayList.size();
    }

    class SummeryDetailViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, quantityTextView, netTextView;

        SummeryDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_li_dialog_client_summery_item_details_name);
            priceTextView = itemView.findViewById(R.id.tv_li_dialog_client_summery_item_details_price);
            quantityTextView = itemView.findViewById(R.id.tv_li_dialog_client_summery_item_details_quantity);
            netTextView = itemView.findViewById(R.id.tv_li_dialog_client_summery_item_details_net);
        }
    }

}


