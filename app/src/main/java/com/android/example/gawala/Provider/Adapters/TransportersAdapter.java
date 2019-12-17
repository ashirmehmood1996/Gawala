package com.android.example.gawala.Provider.Adapters;

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

import com.android.example.gawala.Provider.Models.TransportersModel;
import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class TransportersAdapter extends RecyclerView.Adapter<TransportersAdapter.TransportersHolder> {
    private ArrayList<TransportersModel> transportersModelArrayList;
    private Context context;
    private Callbacks callbacks;
    private boolean isForSelection;

    public TransportersAdapter(ArrayList<TransportersModel> transportersModelArrayList, Activity activity, boolean isForSelection) {
        this.transportersModelArrayList = transportersModelArrayList;
        this.context = activity;
        this.callbacks = (Callbacks) activity;
        this.isForSelection = isForSelection;
    }

    @NonNull
    @Override
    public TransportersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransportersHolder(LayoutInflater.from(context).inflate(R.layout.li_transporters, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransportersHolder holder, int position) {

        TransportersModel transportersModel = transportersModelArrayList.get(position);
        holder.nameTextView.setText(transportersModel.getName());
        holder.numberTextView.setText(transportersModel.getNumber());

        if (transportersModel.getImageUrl().isEmpty()) {
            Glide.with(context).load(R.drawable.ic_person_black_24dp).into(holder.circularImageView);
        } else {
            Glide.with(context).load(transportersModel.getImageUrl()).into(holder.circularImageView);
        }

    }

    @Override
    public int getItemCount() {
        return transportersModelArrayList.size();
    }

    class TransportersHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, numberTextView;
        CircularImageView circularImageView;
        LinearLayout containerLinearLayout;
        Button assignButton;

        TransportersHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_li_transporter_name);
            numberTextView = itemView.findViewById(R.id.tv_li_transporter_number);
            circularImageView = itemView.findViewById(R.id.civ_li_transporter_picture);
            containerLinearLayout = itemView.findViewById(R.id.ll_li_con_prod_container);
            assignButton = itemView.findViewById(R.id.bt_li_transporter_assign);
            containerLinearLayout.setOnClickListener(v -> callbacks.onTransporterClick(getAdapterPosition()));

            if (isForSelection) {
                assignButton.setOnClickListener(v -> callbacks.onAssignButtonClicked(getAdapterPosition()));
            } else {
                assignButton.setVisibility(View.GONE);
            }

        }
    }

    public interface Callbacks {
        void onTransporterClick(int position);

        void onAssignButtonClicked(int positon);

    }
}
