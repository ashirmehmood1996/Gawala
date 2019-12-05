package com.android.example.gawala.Consumer.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Adapters.ClientSummeryAdapter;
import com.android.example.gawala.Generel.Adapters.ClientSummeryItemDetailsAdapter;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.ClientSummeryModel;
import com.android.example.gawala.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ClientSummeryFragment extends DialogFragment implements ClientSummeryAdapter.Callback {
    private static final String ARG_PRODUCER_ID = "producerID";
    private String producerID; // fatch all producers ids dynamically

    private RecyclerView recyclerView;
    private ArrayList<ClientSummeryModel> clientSummeryModelArrayList;
    private ClientSummeryAdapter clientSummeryAdapter;


    private TextView titleTextView, totalItemsTextView, totalCostTextView;
    private TableLayout tableLayout;
    private ImageButton backButton;
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
        loadThisMonthSummary();
        return rootView;
    }


    private void initFields(View rootView) {
        titleTextView = rootView.findViewById(R.id.tv_frag_con_summery_title);
        tableLayout = rootView.findViewById(R.id.tl_frag_client_summery);
        totalCostTextView = rootView.findViewById(R.id.tv_frag_con_summery_total_cost);
        totalItemsTextView = rootView.findViewById(R.id.tv_frag_con_summery_total_items);

        recyclerView = rootView.findViewById(R.id.rv_frag_con_summery);
        backButton = rootView.findViewById(R.id.ib_frag_client_summery_back);

        initializeDialog();

        clientSummeryModelArrayList = new ArrayList<>();
        clientSummeryAdapter = new ClientSummeryAdapter(clientSummeryModelArrayList, getActivity(), this, false);
        recyclerView.setAdapter(clientSummeryAdapter);
    }

    private void attachListeners() {
        backButton.setOnClickListener(v -> dismiss());

    }

    // FIXME: 11/27/2019 //we need to load data from all producer which in this case is not happening the solution to this is tat we will send here all the producerrs ids her if already fetched or will fetch the producer ids first and the qery tthe data for this summery
    private void loadThisMonthSummary() {
        mAlertDialog.show();
        Calendar calendar = Calendar.getInstance();//this calender object contains this month
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//if we keep day of month to 0 then the last day of previous month is also retrieved because the new day hasnot been startted yet
        //fixme fetching  all data which is bad practice later shift to a better solution
        FirebaseDatabase.getInstance().getReference().child("data")
                .child(producerID)
                .child("permanent_data")
                .orderByChild("time_stamp")
                .startAt(calendar.getTimeInMillis())//restricts the data to the values starting  from the specifiend and greater
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (ClientSummeryFragment.this != null) {
                            if (dataSnapshot.exists()) {
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
                                mAlertDialog.dismiss();
                            } else {
                                mAlertDialog.dismiss();
                                Toast.makeText(getContext(), "there was no summary for this month", Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (ClientSummeryFragment.this != null)
                            mAlertDialog.dismiss();

                    }
                });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM, yyyy");
        titleTextView.setText(simpleDateFormat.format(calendar.getTimeInMillis()));

    }

    private void initializeDialog() {
        LinearLayout alertDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_progress, null);
        this.mAlertDialog = new AlertDialog.Builder(getActivity()).setView(alertDialog).setCancelable(false).create();
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

// todo doing now
//                          for another month we need to provide an option for selecting months indeed and showing results accordingly with start at and edn at query