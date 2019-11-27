package com.android.example.gawala.Generel.Adapters;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Generel.Models.GoodModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;

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
        GoodModel currentGood = goodModelArrayList.get(position);
        holder.nameTextView.setText(currentGood.getName());
        holder.priceTextView.setText(currentGood.getPrice() + " PKR");// hard coded for now later we will deal when we add currency suppor
        holder.typeTextView.setText(currentGood.getType());

        if (currentGood.getImage_uri() != null && !currentGood.getImage_uri().isEmpty()) {
            Glide.with(context).load(currentGood.getImage_uri()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_broken_image_black_24dp);
        }

    }

    @Override
    public int getItemCount() {
        return goodModelArrayList.size();
    }

    class GoodsHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, priceTextView, typeTextView;
        LinearLayout goodsContainer;
        ImageView imageView;

        GoodsHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_li_goods_name);
            priceTextView = itemView.findViewById(R.id.tv_li_goods_price);
            typeTextView = itemView.findViewById(R.id.tv_li_goods_type);
            goodsContainer = itemView.findViewById(R.id.ll_li_goods_container);
            imageView = itemView.findViewById(R.id.iv_li_goods_picture);
            goodsContainer.setOnClickListener(v -> callBack.onGoodItemClick(getAdapterPosition()));
        }
    }

    public interface CallBack {

        void onGoodItemClick(int position);
    }

}
