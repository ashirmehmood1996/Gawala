package com.android.example.gawala.Provider.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.example.gawala.R;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    public SpinnerAdapter(Context context, List<String> objects) {
        super(context, 0, objects);
    }

    //for simple display of spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.classes_spinner_list_item, parent, false);
        }
        //get references
        TextView titleTextView = view.findViewById(R.id.tv_spinner_li_title);
        titleTextView.setText(getItem(position));
        return view;
    }

    //for drop down view
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.classes_spinner_list_item, parent, false);
        }
        //get references
        TextView titleTextView = view.findViewById(R.id.tv_spinner_li_title);
        titleTextView.setText(getItem(position));
        return view;
    }
}
