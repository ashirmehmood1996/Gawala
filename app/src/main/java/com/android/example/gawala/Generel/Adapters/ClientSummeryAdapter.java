package com.android.example.gawala.Generel.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Generel.Models.ClientSummeryModel;
import com.android.example.gawala.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ClientSummeryAdapter extends RecyclerView.Adapter<ClientSummeryAdapter.SummeryHolder> {
    private ArrayList<ClientSummeryModel> clientSummeryModelArrayList;
    private Context context;
    private Callback callback;
    private boolean isFromProducer;

    public ClientSummeryAdapter(ArrayList<ClientSummeryModel> clientSummeryModelArrayList, Context context, Callback callback, boolean isFromProducer) {
        this.clientSummeryModelArrayList = clientSummeryModelArrayList;
        this.context = context;
        this.callback = callback;
        this.isFromProducer = isFromProducer;
    }

    @NonNull
    @Override
    public SummeryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new SummeryHolder(LayoutInflater.from(context).inflate(R.layout.li_client_summery, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SummeryHolder holder, int position) {

        ClientSummeryModel clientSummeryModel = clientSummeryModelArrayList.get(position);
        if (isFromProducer) {
            holder.dateTextView.setText(clientSummeryModel.getName());
        } else {
            holder.dateTextView.setText(getFormattedDate(clientSummeryModel.getTime_stamp()));
        }
        holder.itemsTextView.setText(String.format("%d item(s)", clientSummeryModel.getAcquiredGoodModelArrayList().size()));
        holder.costTextView.setText(clientSummeryModel.getTotalCost() + " PKR");

    }

    private String getFormattedDate(long timeInMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM yyyy ");
        return simpleDateFormat.format(timeInMilliseconds);
    }


    @Override
    public int getItemCount() {
        return clientSummeryModelArrayList.size();
    }

    class SummeryHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, itemsTextView, costTextView;
        TableRow containerTableRow;


        SummeryHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.tv_li_client_summery_date);
            itemsTextView = itemView.findViewById(R.id.tv_li_client_summery_items);
            costTextView = itemView.findViewById(R.id.tv_li_client_summery_cost);
            containerTableRow = itemView.findViewById(R.id.tr_li_client_summery_container);
            containerTableRow.setOnClickListener(v -> {
                callback.onTableRowClick(getAdapterPosition());
            });
        }
    }

    public interface Callback {
        void onTableRowClick(int position);
    }
}
