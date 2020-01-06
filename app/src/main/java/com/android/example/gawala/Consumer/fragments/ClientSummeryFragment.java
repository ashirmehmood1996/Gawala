package com.android.example.gawala.Consumer.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.DialogFragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Adapters.ClientSummeryAdapter;
import com.android.example.gawala.Generel.Adapters.ClientSummeryItemDetailsAdapter;
import com.android.example.gawala.Generel.Fraagments.MonthYearPickerDialog;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.ClientSummeryModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.android.example.gawala.Generel.Activities.MainActivity.rootRef;


public class ClientSummeryFragment extends DialogFragment implements ClientSummeryAdapter.Callback {
    private static final String ARG_PRODUCER_ID = "producerID";
    private static final String TAG_MONTH_YEAR_PICKER = "monthYearPickerTag";
    private String producerID; // fatch all producers ids dynamically

    private RecyclerView recyclerView;
    private ArrayList<ClientSummeryModel> clientSummeryModelArrayList;
    private ClientSummeryAdapter clientSummeryAdapter;


    private TextView titleTextView, totalItemsTextView, totalCostTextView;
    private TableLayout tableLayout;
    private RelativeLayout emptyViewcontainer;
    private ImageButton backButton;
    private String myId;
    private AlertDialog mProgressDialog;
    private ImageButton monthPickerButton;
    private Calendar selectedMonthCalendar;


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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);
        if (getArguments() != null) {
            producerID = getArguments().getString(ARG_PRODUCER_ID);
        }
        myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_client_summery, container, false);
        initFields(rootView);
        attachListeners();


        selectedMonthCalendar = Calendar.getInstance();//this calender object contains this month
        selectedMonthCalendar.set(Calendar.MILLISECOND, 0);
        selectedMonthCalendar.set(Calendar.SECOND, 0);
        selectedMonthCalendar.set(Calendar.MINUTE, 0);
        selectedMonthCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);//if we keep day of month to 0 then the last day of previous month is also retrieved because the new day hasnot been startted yet
        loadThisMonthSummary();

        return rootView;
    }


    private void initFields(View rootView) {
        titleTextView = rootView.findViewById(R.id.tv_frag_con_summery_title);
        tableLayout = rootView.findViewById(R.id.tl_frag_client_summery);
        emptyViewcontainer = rootView.findViewById(R.id.rl_frag_client_summery_empty_view_container);
        totalCostTextView = rootView.findViewById(R.id.tv_frag_con_summery_total_cost);
        totalItemsTextView = rootView.findViewById(R.id.tv_frag_con_summery_total_items);

        recyclerView = rootView.findViewById(R.id.rv_frag_con_summery);
        backButton = rootView.findViewById(R.id.ib_frag_client_summery_back);
        monthPickerButton = rootView.findViewById(R.id.ib_frag_client_summery_month_picker);

        initializeDialog();

        clientSummeryModelArrayList = new ArrayList<>();
        clientSummeryAdapter = new ClientSummeryAdapter(clientSummeryModelArrayList, getActivity(), this, false);
        recyclerView.setAdapter(clientSummeryAdapter);
    }

    private void attachListeners() {
        backButton.setOnClickListener(v -> dismiss());
        monthPickerButton.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            MonthYearPickerDialog monthYearPickerDialog = (MonthYearPickerDialog) getChildFragmentManager().findFragmentByTag(TAG_MONTH_YEAR_PICKER);

            if (monthYearPickerDialog != null) {
                fragmentTransaction.remove(monthYearPickerDialog);
            }

            monthYearPickerDialog = MonthYearPickerDialog.newInstance(selectedMonthCalendar.get(Calendar.MONTH), selectedMonthCalendar.get(Calendar.YEAR));
            monthYearPickerDialog.setListener((month, year) -> {
                selectedMonthCalendar.set(Calendar.MONTH, month);
                selectedMonthCalendar.set(Calendar.YEAR, year);
                loadThisMonthSummary();
            });

            monthYearPickerDialog.show(fragmentTransaction, TAG_MONTH_YEAR_PICKER);

        });
    }

    private void loadThisMonthSummary() {
        Calendar endMonthCalendar = Calendar.getInstance();
        endMonthCalendar.set(Calendar.MILLISECOND, 0);
        endMonthCalendar.set(Calendar.SECOND, 0);
        endMonthCalendar.set(Calendar.MINUTE, 0);
        endMonthCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);//if we keep day of month to 0 then the last day of previous month is also retrieved because the new day hasnot been startted yet


        endMonthCalendar.set(Calendar.MONTH, selectedMonthCalendar.get(Calendar.MONTH) + 1);
        endMonthCalendar.set(Calendar.YEAR, selectedMonthCalendar.get(Calendar.YEAR));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM, yyyy");
        titleTextView.setText(simpleDateFormat.format(selectedMonthCalendar.getTimeInMillis()));
//        titleTextView.append("\n" + new SimpleDateFormat("hh:mm: a dd,MMMM, yyyy").format(endMonthCalendar.getTimeInMillis()));

        mProgressDialog.show();
        //fixme LATER when systme expands fetching  all data which is bad practice later shift to a better solution
        rootRef.child("data")
                .child(producerID)
                .child("permanent_data")
                .orderByChild("time_stamp")
                .startAt(selectedMonthCalendar.getTimeInMillis())//restricts the data to the values starting  from the specifiend and greater
                .endAt(endMonthCalendar.getTimeInMillis())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (ClientSummeryFragment.this != null) {
                            clientSummeryModelArrayList.clear();
                            if (dataSnapshot.exists()) {
                                tableLayout.setVisibility(View.VISIBLE);
                                emptyViewcontainer.setVisibility(View.GONE);
                                int totalItems = 0;
                                int totalCost = 0;
                                for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {

                                    long timeStamp = sessionSnapShot.child("time_stamp").getValue(Long.class);

                                    DataSnapshot thisClientData = sessionSnapShot.child("clients").child(myId);


                                    ArrayList<AcquiredGoodModel> acquiredGoodModels = new ArrayList<>();
                                    for (DataSnapshot aquiredGoodSnap : thisClientData.child("goods").getChildren()) {
                                        acquiredGoodModels.add(aquiredGoodSnap.getValue(AcquiredGoodModel.class));
                                    }
                                    ClientSummeryModel clientSummeryModel = new ClientSummeryModel(myId, "not needed", acquiredGoodModels);
                                    clientSummeryModelArrayList.add(clientSummeryModel);
                                    clientSummeryModel.setTime_stamp(timeStamp);
                                    clientSummeryAdapter.notifyDataSetChanged();


//                                    TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.li_client_summery, null);
//
//                                    TextView dateView = tableRow.findViewById(R.id.tv_li_client_summery_date);
//                                    String time = getFormattedDate(timeStamp);
//                                    dateView.setText(time);
//
//                                    TextView volumeView = tableRow.findViewById(R.id.tv_li_client_summery_items);
//                                    volumeView.setText(String.format("%d item(s)", clientSummeryModel.getAcquiredGoodModelArrayList().size()));
//
//                                    TextView costView = tableRow.findViewById(R.id.tv_li_client_summery_cost);
//                                    costView.setText(clientSummeryModel.getTotalCost() + " PKR");
//                                    tableLayout.addView(tableRow);

                                    totalItems += clientSummeryModel.getAcquiredGoodModelArrayList().size();
                                    totalCost += clientSummeryModel.getTotalCost();
                                }

//                                TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.li_client_summery, null);
//                                TextView dateView = tableRow.findViewById(R.id.tv_li_client_summery_date);
//                                dateView.setText(Html.fromHtml("<b>TOTAL</b>"));
//                                TextView volumeView = tableRow.findViewById(R.id.tv_li_client_summery_items);
                                totalItemsTextView.setText(String.format("%d item(s)", totalItems));
                                totalCostTextView.setText(totalCost + " PKR");
//                                volumeView.setText(Html.fromHtml("<b>" + totalItems + " item(s)</b>"));
//                                TextView costView = tableRow.findViewById(R.id.tv_li_client_summery_cost);
//                                costView.setText(Html.fromHtml("<b>" + totalCost + " PKR</b>"));
//                                tableLayout.addView(tableRow);
                            } else {
                                Toast.makeText(getContext(), "there was no summary for this month", Toast.LENGTH_LONG).show();
                                tableLayout.setVisibility(View.GONE);
                                emptyViewcontainer.setVisibility(View.VISIBLE);
                            }
                            mProgressDialog.dismiss();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (ClientSummeryFragment.this != null)
                            mProgressDialog.dismiss();
                    }
                });
    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mProgressDialog = new AlertDialog.Builder(getActivity()).setView(alertDialog).setCancelable(false).create();
    }

    private String getFormattedDate(long timeInMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM yyyy ");
        return simpleDateFormat.format(timeInMilliseconds);
    }

    @Override
    public void onTableRowClick(int position) {


        showDetailsDialog(position);
    }

    private void showDetailsDialog(int position) {
        ClientSummeryModel clientSummeryModel = clientSummeryModelArrayList.get(position);
        if (clientSummeryModel.getAcquiredGoodModelArrayList() == null ||
                clientSummeryModel.getAcquiredGoodModelArrayList() != null && clientSummeryModel.getAcquiredGoodModelArrayList().isEmpty()) {
            Toast.makeText(getActivity(), "some item is clicked", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "No Details Available", Toast.LENGTH_LONG).show();
            return;
        }
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_client_summery_item_detail, null);
        RecyclerView recyclerView = linearLayout.findViewById(R.id.rv_dialog_client_summery_detail);
        TextView totalTextView = linearLayout.findViewById(R.id.tv_dialog_client_summery_detail_total_price);
        totalTextView.setText(String.format("%s PKR", clientSummeryModel.getTotalCost()));
        TextView dateTextView = linearLayout.findViewById(R.id.tv_dialog_client_summery_detail_date);
        dateTextView.setText(String.format(getFormattedDate(clientSummeryModel.getTime_stamp())));


        ClientSummeryItemDetailsAdapter clientSummeryItemDetailsAdapter = new ClientSummeryItemDetailsAdapter(clientSummeryModel.getAcquiredGoodModelArrayList(), getActivity());
        recyclerView.setAdapter(clientSummeryItemDetailsAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("ok", null);
        builder.setView(linearLayout);
        builder.show();
    }
}