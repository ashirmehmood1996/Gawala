package com.android.example.gawala.Generel.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class GoodsAdapter extends RecyclerView.Adapter<GoodsAdapter.GoodsHolder> {
    private ArrayList<GoodModel> goodModelArrayList;
    private Context context;
    private CallBack callBack;

    public GoodsAdapter(ArrayList<GoodModel> goodModelArrayList, Activity activity) {
        this.goodModelArrayList = goodModelArrayList;
        this.context = activity;
        this.callBack = (CallBack) activity;
    }

    @NonNull
    @Override
    public GoodsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GoodsHolder(LayoutInflater.from(context)
                .inflate(R.layout.li_producer_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GoodsHolder holder, int position) {
        GoodModel currentGood=goodModelArrayList.get(position);
        holder.nameTextView.setText(currentGood.getName());
        holder.priceTextView.setText(currentGood.getPrice() + " PKR");// hard coded for now later we will deal when we add currency suppor
        holder.typeTextView.setText(currentGood.getType());
    }

    @Override
    public int getItemCount() {
        return goodModelArrayList.size();
    }

    class GoodsHolder extends RecyclerView.ViewHolder {

        TextView nameTextView,priceTextView,typeTextView;
        LinearLayout goodsContainer;

        GoodsHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView=itemView.findViewById(R.id.tv_li_goods_name);
            priceTextView=itemView.findViewById(R.id.tv_li_goods_price);
            typeTextView=itemView.findViewById(R.id.tv_li_goods_type);
            goodsContainer =itemView.findViewById(R.id.ll_li_goods_container);
            goodsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack.onGoodItemClick(getAdapterPosition());
                }
            });
        }
    }

    public interface CallBack {

        void onGoodItemClick(int position);
    }

}
