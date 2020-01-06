package com.android.example.gawala.Generel.Fraagments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.example.gawala.R;

import java.util.Calendar;

public class MonthYearPickerDialog extends DialogFragment {
    //modify this according to the need and understand too
    private static final int MIN_YEAR = 2010;
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private Callback callback;
    private int month, year;

    public static MonthYearPickerDialog newInstance(int month, int year) {

        Bundle args = new Bundle();
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_YEAR, year);

        MonthYearPickerDialog fragment = new MonthYearPickerDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            month = getArguments().getInt(ARG_MONTH);
            year = getArguments().getInt(ARG_YEAR);
        }
    }

    public void setListener(Callback listener) {
        this.callback = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.my_date_picker_dialog, null);
        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue( month+1);//adding one becuase the count start with 0 and alater we subtract that one at the dismiss time
//        monthPicker.setValue(cal.get(Calendar.MONTH));

        int max = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(max);
        yearPicker.setValue(year);
//        yearPicker.setValue(max);

        builder.setTitle("Pick up the Month and year")
                .setView(dialog)
                // Add action buttons
                .setPositiveButton("OK", (dialog1, id) -> callback.onDateSelected(monthPicker.getValue() - 1, yearPicker.getValue()))
                .setNegativeButton("cancel", (dialog12, id) -> MonthYearPickerDialog.this.getDialog().cancel());

        return builder.create();
    }

    public interface Callback {
        void onDateSelected(int month, int year);
    }
}
