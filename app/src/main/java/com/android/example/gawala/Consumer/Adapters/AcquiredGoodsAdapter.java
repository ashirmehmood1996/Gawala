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

import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class AcquiredGoodsAdapter extends RecyclerView.Adapter<AcquiredGoodsAdapter.AquiredgoodsHolder> {
    private ArrayList<AcquiredGoodModel> aquiredGoodsArrayList;
    private Context context;
    private Callback callback;

    public AcquiredGoodsAdapter(ArrayList<AcquiredGoodModel> aquiredGoodsArrayList, Activity activity) {
        this.aquiredGoodsArrayList = aquiredGoodsArrayList;
        this.context = activity;
        this.callback= (Callback) activity;
    }

    @NonNull
    @Override
    public AquiredgoodsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AquiredgoodsHolder(LayoutInflater.from(context).inflate(R.layout.li_acquired_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AquiredgoodsHolder holder, int position) {
        holder.nameTextvew.setText(aquiredGoodsArrayList.get(position).getGoodModel().getName());
        holder.typeTextView.setText(aquiredGoodsArrayList.get(position).getGoodModel().getType());
        holder.priceTextView.setText(String.format("%s PKR", aquiredGoodsArrayList.get(position).getGoodModel().getPrice()));
//        holder.producerNameTextView.setText("prdocer"); later

    }

    @Override
    public int getItemCount() {
        return aquiredGoodsArrayList.size();
    }

    class AquiredgoodsHolder extends RecyclerView.ViewHolder {
        LinearLayout containerLayout;
        TextView nameTextvew, priceTextView, typeTextView, producerNameTextView;

        public AquiredgoodsHolder(@NonNull View itemView) {
            super(itemView);
            containerLayout = itemView.findViewById(R.id.ll_li_acquired_goods_container);
            nameTextvew = itemView.findViewById(R.id.tv_li_acquired_goods_name);
            priceTextView = itemView.findViewById(R.id.tv_li_acquired_goods_price);
            typeTextView = itemView.findViewById(R.id.tv_li_acquired_goods_type);
            producerNameTextView = itemView.findViewById(R.id.tv_li_acquired_goods_producer_name);

            containerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onAcquiredGoodClicked(getAdapterPosition());
                }
            });

        }
    }

    public interface Callback {

        void onAcquiredGoodClicked(int pos);

    }
}
