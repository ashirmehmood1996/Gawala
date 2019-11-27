package com.android.example.gawala.Consumer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Consumer.Models.ProducerModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.util.ArrayList;

public class ProducersAdapter extends RecyclerView.Adapter<ProducersAdapter.ProducerHolder> {
    private ArrayList<ProducerModel> producerModelArrayList;
    private Context context;
    private CallBack callBack;

    public ProducersAdapter(ArrayList<ProducerModel> producerModels, Activity activity) {
        this.producerModelArrayList = producerModels;
        this.context = activity;
        this.callBack = (CallBack) activity;


    }

    @NonNull
    @Override
    public ProducerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProducerHolder(LayoutInflater.from(context).inflate(R.layout.li_producers, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProducerHolder holder, int position) {
        ProducerModel producerModel =producerModelArrayList.get(position);
        holder.nameTextView.setText(producerModel.getName());
        holder.numberTextView.setText(producerModel.getNumber());
        if (!producerModel.getImageUri().isEmpty()) {
            Glide.with(context).load(producerModel.getImageUri()).into(holder.circularImageView);
        }


//        switch (producerModel.getStatus()) {
//            case ProducerModel.STATUS_NEUTRAL:
//                holder.sendRequestButton.setText("send request");
//                break;
//            case ProducerModel.REQUEST_ACCEPTED:
//                holder.sendRequestButton.setText("remove producer");
//                break;
//            case ProducerModel.REQUEST_SENT:
//                holder.sendRequestButton.setText("cancel request");
//                break;
//        }
    }

    @Override
    public int getItemCount() {
        return producerModelArrayList.size();
    }

    class ProducerHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, numberTextView;
//        Button  sendRequestButton;
        LinearLayout mainContainerLinearLayout;
        private CircularImageView circularImageView;

        public ProducerHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView=itemView.findViewById(R.id.tv_li_prod_name);
            numberTextView =itemView.findViewById(R.id.tv_li_prod_number);
//            sendRequestButton=itemView.findViewById(R.id.bt_li_prod_send_request);
            mainContainerLinearLayout=itemView.findViewById(R.id.ll_li_prod_container);
            circularImageView=itemView.findViewById(R.id.civ_li_prod_picture);

//            sendRequestButton.setOnClickListener(v -> callBack.onSendRequest(getAdapterPosition()));
            mainContainerLinearLayout.setOnClickListener(v -> callBack.onProducerItemClcik(getAdapterPosition()));
        }
    }

    public interface CallBack {
//        void onSendRequest(int pos);
        void onProducerItemClcik(int pos);
    }

}


