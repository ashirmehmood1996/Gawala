package com.android.example.gawala.Transporter.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Provider.Models.ConsumerModel;
import com.android.example.gawala.R;
import com.android.example.gawala.Transporter.Services.RideService;
import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class AciveRideStopsAdaper extends RecyclerView.Adapter<AciveRideStopsAdaper.ActiverideViewHolder> {
    private Context context;
    private ArrayList<ConsumerModel> consumerModelArrayList;
//    private Callbacks callbacks;

    /**
     * @param context
     * @param consumerModelArrayList the llist of consumers to which goods are needed to be sent
     */
    public AciveRideStopsAdaper(Context context, ArrayList<ConsumerModel> consumerModelArrayList/*, Callbacks callbacks*/) {
        this.context = context;
        this.consumerModelArrayList = consumerModelArrayList;
//        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public ActiverideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActiverideViewHolder(LayoutInflater.from(context).inflate(R.layout.li_active_ride_stops, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ActiverideViewHolder holder, int position) {
        ConsumerModel consumerModel = consumerModelArrayList.get(position);
        holder.bind(consumerModel);
//        float milkAmount = consumerModel.getAmountOfMilk();


    }

    @Override
    public int getItemCount() {
        return consumerModelArrayList.size();
    }

    class ActiverideViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerLinearLayout, goodsItemInfoContainer;
        TextView nameTextView, locationTextView, statusTextView, priorityTextView;


        ActiverideViewHolder(@NonNull View itemView) {
            super(itemView);

            containerLinearLayout = itemView.findViewById(R.id.ll_li_active_ride_container);
            goodsItemInfoContainer = itemView.findViewById(R.id.ll_li_active_rider_items_info_container);
            nameTextView = itemView.findViewById(R.id.tv_li_active_ride_client_name);
            locationTextView = itemView.findViewById(R.id.tv_li_active_ride_client_address);
            statusTextView = itemView.findViewById(R.id.tv_li_active_ride_client_status);
            priorityTextView = itemView.findViewById(R.id.tv_li_active_ride_priority);
            atachListeners();
        }

        private void atachListeners() {
            containerLinearLayout.setOnClickListener(v -> Toast.makeText(context, String.format("%d position clicked", getAdapterPosition()), Toast.LENGTH_SHORT).show());
        }

        void bind(ConsumerModel consumerModel) {

            String name = consumerModel.getName();
            String location;

            if (consumerModel.getLocationName() == null || consumerModel.getLocationName().isEmpty()) {
                location = consumerModel.getLatitude() + " " + consumerModel.getLongitude();
            } else {
                location = consumerModel.getLocationName();
            }


            priorityTextView.setText((getAdapterPosition() + 1) + "");

            nameTextView.setText(name);
            locationTextView.setText(location);

            boolean status = consumerModel.isDelivered();


            GradientDrawable gradientDrawable = (GradientDrawable) priorityTextView.getBackground();

            goodsItemInfoContainer.removeAllViews();// to remove previously added vies if any
            if (RideService.activeStopPosition != getAdapterPosition()) {
                goodsItemInfoContainer.setVisibility(View.GONE);


                if (status) {
                    statusTextView.setText("Delivered");

                    statusTextView.setTextColor(Color.BLUE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        gradientDrawable.setTint(Color.BLUE);
                    }
                } else {
                    statusTextView.setText("Pending");
                    statusTextView.setTextColor(Color.GRAY);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        gradientDrawable.setTint(Color.GRAY);
                    }
                }

            } else {
                goodsItemInfoContainer.setVisibility(View.VISIBLE);

                ArrayList<AcquiredGoodModel> acquiredGoodModelArrayList = consumerModel.getDemandArray();
                for (AcquiredGoodModel acquiredGoodModel : acquiredGoodModelArrayList) {
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(itemView.getContext()).inflate(R.layout.li_good_to_carry, null);

                    ImageView imageView = linearLayout.findViewById(R.id.iv_li_good_to_carry_image);
                    TextView nameTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_name);
                    TextView demandTextView = linearLayout.findViewById(R.id.tv_li_good_to_carry_units);

                    Glide.with(itemView.getContext())
                            .load(acquiredGoodModel.getGoodModel().getImage_uri())
                            .into(imageView);
                    nameTextView.setText(acquiredGoodModel.getGoodModel().getName());
                    demandTextView.setText(acquiredGoodModel.getDemand() + " " + acquiredGoodModel.getGoodModel().getUnit());

                    linearLayout.setBackground(null);
                    goodsItemInfoContainer.addView(linearLayout);
                }

                // here we make some view visible adn make it invisible in the previous

                statusTextView.setText("Carrying");

                statusTextView.setTextColor(context.getResources().getColor(R.color.colorGreen));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gradientDrawable.setTint(context.getResources().getColor(R.color.colorGreen));
                }
            }
        }

    }
//
//    public class Callbacks {
//
//    }
}
