package com.android.example.gawala.Producer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Producer.Models.ProducerSummeryModel;
import com.android.example.gawala.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ProducerSummeryAdapter extends RecyclerView.Adapter<ProducerSummeryAdapter.SummeryHolder> {
    private Context context;
    private ArrayList<ProducerSummeryModel> producerSummeryModelArrayList;


    public ProducerSummeryAdapter(Context context, ArrayList<ProducerSummeryModel> producerSummeryModelArrayList) {
        this.producerSummeryModelArrayList = producerSummeryModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SummeryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SummeryHolder(LayoutInflater.from(context)
                .inflate(R.layout.li_producer_summery, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SummeryHolder holder, int position) {
        ProducerSummeryModel producerSummeryModel = producerSummeryModelArrayList.get(position);
        holder.volumeTV.setText(producerSummeryModel.getTotalMilkVolume() + " litre(s)");
        holder.costTV.setText(producerSummeryModel.getTotalMilkamount() + " PKR");
        holder.numberOfClientsTV.setText(producerSummeryModel.getClientArrayList().size() + "");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM yyyy");
        simpleDateFormat.format(producerSummeryModel.getTimeStamp());
        holder.dateTV.setText(simpleDateFormat.format(producerSummeryModel.getTimeStamp()));

        // TODO: 9/23/2019 need to send to another fragemt for session details

    }

    @Override
    public int getItemCount() {
        return producerSummeryModelArrayList.size();
    }

    class SummeryHolder extends RecyclerView.ViewHolder {
        TextView dateTV, volumeTV, costTV, numberOfClientsTV;

        public SummeryHolder(@NonNull View itemView) {
            super(itemView);
            dateTV = itemView.findViewById(R.id.tv_li_pro_summery_date);
            volumeTV = itemView.findViewById(R.id.tv_li_pro_summery_volume);
            costTV = itemView.findViewById(R.id.tv_li_pro_summery_cost);
            numberOfClientsTV = itemView.findViewById(R.id.tv_li_pro_summery_clients);
        }
    }
}
