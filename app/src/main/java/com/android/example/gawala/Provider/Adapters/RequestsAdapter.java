package com.android.example.gawala.Provider.Adapters;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.example.gawala.Transporter.Interfaces.RequestsAdapterCallbacks;
import com.android.example.gawala.Provider.Models.RequestModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {
    private ArrayList<RequestModel> requestModelArrayList;
    private Context context;

    private RequestsAdapterCallbacks requestsAdapterCallbacks;

    public RequestsAdapter(ArrayList<RequestModel> requestModelArrayList, Activity activity, RequestsAdapterCallbacks requestsAdapterCallbacks) {
        this.requestModelArrayList = requestModelArrayList;
        this.context = activity;
        this.requestsAdapterCallbacks = requestsAdapterCallbacks;

    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RequestViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.li_consumer_request, viewGroup, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, final int i) {// FIXME: 7/13/2019 LATER find out the drawbacks of making it final
        RequestModel currentRequestModel = requestModelArrayList.get(i);
        String name = currentRequestModel.getName();
        double lat = Double.parseDouble(currentRequestModel.getLat());
        double lng = Double.parseDouble(currentRequestModel.getLng());
        if (!currentRequestModel.getImageUrl().isEmpty()) {
            Glide.with(context).load(currentRequestModel.getImageUrl()).into(holder.circularImageView);
        } else {
            holder.circularImageView.setBackgroundResource(R.drawable.ic_person_black_24dp);
        }
    /*    if (currentRequestModel.getAddress() == null ||
                (currentRequestModel.getAddress() != null && currentRequestModel.getAddress().isEmpty())) {
            new GeoCoderAsyncTask(context) {
                @Override
                protected void onPostExecute(Address address) {
                    super.onPostExecute(address);
                    if (address != null) {
                        String addressStr = address.getAddressLine(0);
                        currentRequestModel.setAddress(addressStr);
                        RequestsAdapter.this.notifyDataSetChanged();
                    }
//                    holder.locationTextView.setText(currentRequestModel.getAddress());
                }
            }.execute(new LatLng(lat, lng));
        } else {*/
        holder.locationTextView.setText(currentRequestModel.getAddress());
//        }
        String timeinMilliSecStr = currentRequestModel.getTime_stamp();
        long time = Long.parseLong(timeinMilliSecStr);
        SimpleDateFormat formater = new SimpleDateFormat("dd, MMM yyyy\n hh:mm:ss a");
        String formattedTime = formater.format(time);
        holder.timestampTextView.setText(formattedTime);
        holder.nameTextView.setText(name);


    }


    @Override
    public int getItemCount() {
        return requestModelArrayList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView, timestampTextView;
        Button acceptButton, rejectButton;
        LinearLayout requestContainer;
        CircularImageView circularImageView;


        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            requestContainer = itemView.findViewById(R.id.ll_li_requests_container);
            nameTextView = itemView.findViewById(R.id.tv_li_request_name);
            locationTextView = itemView.findViewById(R.id.tv_li_request_location);
            timestampTextView = itemView.findViewById(R.id.tv_li_request_time);
            acceptButton = itemView.findViewById(R.id.bt_li_request_accept);
            rejectButton = itemView.findViewById(R.id.bt_li_request_reject);
            circularImageView = itemView.findViewById(R.id.civ_li_request_picture);

            acceptButton.setOnClickListener(v -> requestsAdapterCallbacks.onRequestAccepted(getAdapterPosition()));
            rejectButton.setOnClickListener(v -> requestsAdapterCallbacks.onRequestCancel(getAdapterPosition()));
            requestContainer.setOnClickListener(v -> {
                requestsAdapterCallbacks.onRequestClientClicked(getAdapterPosition());
            });
        }
    }
}
