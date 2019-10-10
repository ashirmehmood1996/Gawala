package com.android.example.gawala.Consumer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.R;

import java.util.ArrayList;

public class ConnectedProducersAdapter extends RecyclerView.Adapter<ConnectedProducersAdapter.ProducerHolder> {
    private ArrayList<String> producersArrayList;
    private Context context;
    private Callback callback;

    public ConnectedProducersAdapter(ArrayList<String> producersArrayList, Activity activity) {
        this.producersArrayList = producersArrayList;
        this.context = activity;
        this.callback = (Callback) activity;
    }

    @NonNull
    @Override
    public ProducerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProducerHolder(LayoutInflater.from(context)
                .inflate(R.layout.li_connected_producers, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ProducerHolder holder, int position) {
        holder.nameTextView.setText(producersArrayList.get(position));

    }

    @Override
    public int getItemCount() {
        return producersArrayList.size();
    }

    class ProducerHolder extends RecyclerView.ViewHolder {
        LinearLayout producerConatiner;
        TextView nameTextView,numberTextView;

        ProducerHolder(@NonNull View itemView) {
            super(itemView);
            producerConatiner=itemView.findViewById(R.id.ll_li_con_prod_container);
            nameTextView=itemView.findViewById(R.id.tv_li_con_prod_name);
            numberTextView=itemView.findViewById(R.id.tv_li_con_prod_number);

            producerConatiner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onconnectedProducerClick(getAdapterPosition());
                }
            });

        }
    }

    public interface Callback {

        void onconnectedProducerClick(int pos);
    }
}
