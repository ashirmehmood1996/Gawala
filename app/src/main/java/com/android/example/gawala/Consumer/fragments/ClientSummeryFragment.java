package com.android.example.gawala.Consumer.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ClientSummeryFragment extends Fragment {
    private static final String ARG_PRODUCER_ID = "producerID";
    private String producerID;


    private TextView titleTextView;
    private TableLayout tableLayout;
    private String myId;
    private AlertDialog mAlertDialog;


    public ClientSummeryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param parentId parent id
     * @return A new instance of fragment ClientSummeryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClientSummeryFragment newInstance(String parentId) {
        ClientSummeryFragment fragment = new ClientSummeryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCER_ID, parentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            producerID = getArguments().getString(ARG_PRODUCER_ID);
        }
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initializeDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_client_summery, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Summery");
        initFields(rootView);
        loadThisMonthSummary();
        return rootView;
    }

    private void initFields(View rootView) {

        titleTextView = rootView.findViewById(R.id.tv_frag_con_summery_title);
        tableLayout = rootView.findViewById(R.id.tl_frag_client_summery);
    }


    private void loadThisMonthSummary() {
        mAlertDialog.show();
        Calendar calendar = Calendar.getInstance();//this calender object contains this month
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//if we keep day of month to 0 then the last day of previous month is also retrieved because the new day hasnot been startted yet
        //fixme fetching  all data ehich is bad practive later shift to a better solution
        FirebaseDatabase.getInstance().getReference().child("data")
                .child(producerID)
                .child("permanent_data")
                .orderByChild("time_stamp")
                .startAt(calendar.getTimeInMillis())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            float totalVolume = 0.0f;
                            int totalCost = 0;
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                DataSnapshot thisClientData = data.child("clients").child(myId);
                                long timeStamp = data.child("time_stamp").getValue(Long.class);
                                int milkPrice = data.child("milk_price").getValue(Integer.class);

                                TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row_client_summery, null);

                                TextView dateView = tableRow.findViewById(R.id.tv_tr_date);
                                String time = getFormattedData(timeStamp);
                                dateView.setText(time);

                                TextView volumeView = tableRow.findViewById(R.id.tv_tr_volume);
                                float volume = thisClientData.child("milk_amount").getValue(Float.class); // FIXME: 10/8/2019 solve the issue
                                volumeView.setText(volume + " litre(s)");
                                totalVolume += volume;

                                TextView costView = tableRow.findViewById(R.id.tv_tr_cost);
                                int cost = (int) (volume * milkPrice);
                                costView.setText(cost + " PKR");
                                totalCost += cost;

                                tableLayout.addView(tableRow);
                            }

                            TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row_client_summery, null);
                            TextView dateView = tableRow.findViewById(R.id.tv_tr_date);
                            dateView.setText(Html.fromHtml("<b>TOTAL</b>"));
                            TextView volumeView = tableRow.findViewById(R.id.tv_tr_volume);
                            volumeView.setText(Html.fromHtml("<b>" + totalVolume + " Litre(s)</b>"));
                            TextView costView = tableRow.findViewById(R.id.tv_tr_cost);
                            costView.setText(Html.fromHtml("<b>" + totalCost + " PKR</b>"));
                            tableLayout.addView(tableRow);
                            mAlertDialog.dismiss();
                        }else {
                            mAlertDialog.dismiss();
                            Toast.makeText(getContext(), "there was no summary for this month", Toast.LENGTH_LONG).show();
                            getActivity().getSupportFragmentManager().beginTransaction().remove(ClientSummeryFragment.this).commit();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mAlertDialog.dismiss();

                    }
                });
    }



    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(getActivity()).setView(alertDialog).setCancelable(false).create();
    }

    @Override
    public void onDetach() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Consumer");

        super.onDetach();
    }

    private String getFormattedData(long timeInMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM yyyy ");
        return simpleDateFormat.format(timeInMilliseconds);
    }
}
