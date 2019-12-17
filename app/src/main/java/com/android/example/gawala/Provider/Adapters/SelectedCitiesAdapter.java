package com.android.example.gawala.Provider.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Transporter.Models.CityModel;
import com.android.example.gawala.R;

import java.util.ArrayList;

public class SelectedCitiesAdapter extends RecyclerView.Adapter<SelectedCitiesAdapter.CitiesHolder> {
    private ArrayList<CityModel> cityModelArrayList;
    private Context context;
    private Callback callback;

    public SelectedCitiesAdapter(ArrayList<CityModel> cityModelArrayList, DialogFragment dialogFragment) {
        this.cityModelArrayList = cityModelArrayList;
        this.context = dialogFragment.getActivity();
        this.callback = (Callback) dialogFragment;
    }

    @NonNull
    @Override
    public CitiesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CitiesHolder(LayoutInflater.from(context).inflate(R.layout.li_edit_cities, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CitiesHolder holder, int position) {

        holder.cityNameTextView.setText(String.format("%s, %s", cityModelArrayList.get(position).getCity(),
                cityModelArrayList.get(position).getCountry()));

    }

    @Override
    public int getItemCount() {
        return cityModelArrayList.size();
    }

    class CitiesHolder extends RecyclerView.ViewHolder {
        TextView cityNameTextView;
        ImageButton removeCityImageButton;

        CitiesHolder(@NonNull View itemView) {
            super(itemView);
            cityNameTextView = itemView.findViewById(R.id.tv_li_edit_cities_name);
            removeCityImageButton = itemView.findViewById(R.id.ib_li_edit_cities_remove);
            removeCityImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onRemoveCity(getAdapterPosition());
                }
            });
        }
    }

    public interface Callback {
        void onRemoveCity(int position);
    }
}
