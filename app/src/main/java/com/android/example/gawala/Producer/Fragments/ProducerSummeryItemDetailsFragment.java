package com.android.example.gawala.Producer.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.gawala.Generel.Adapters.ClientSummeryAdapter;
import com.android.example.gawala.Generel.Adapters.ClientSummeryItemDetailsAdapter;
import com.android.example.gawala.Generel.Models.AcquiredGoodModel;
import com.android.example.gawala.Generel.Models.ClientSummeryModel;
import com.android.example.gawala.Producer.Models.ProducerSummeryModel;
import com.android.example.gawala.R;

import java.text.SimpleDateFormat;

public class ProducerSummeryItemDetailsFragment extends DialogFragment implements ClientSummeryAdapter.Callback {
    private TextView titleTextView, totalAmountTextView;
    private ImageButton backImageButton;
    private CallBack callback;
    private ProducerSummeryModel producerSummeryModel;
    private RecyclerView recyclerView;
    private ClientSummeryAdapter clientSummeryAdapter;

    public ProducerSummeryItemDetailsFragment() {
        // Required empty public constructor
    }

    public static ProducerSummeryItemDetailsFragment newInstance() {
        return new ProducerSummeryItemDetailsFragment();
    }

    public void setCallback(CallBack callbacks) {
        this.callback = callbacks;

    }

    public void setProducerSummeryModel(ProducerSummeryModel producerSummeryModel) {
        this.producerSummeryModel = producerSummeryModel;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_producer_summery_item_details, container, false);
        initFields(rootView);
        attachListeners();

        setData();
        return rootView;

    }

    private void initFields(View rootView) {
        titleTextView = rootView.findViewById(R.id.tv_frag_prod_summery_item_detail_title);
        totalAmountTextView = rootView.findViewById(R.id.tv_frag_prod_summery_item_detail_total_cost);
        backImageButton = rootView.findViewById(R.id.iv_frag_prod_summery_item_detail_back);

        recyclerView = rootView.findViewById(R.id.rv_frag_prod_summery_item_detail);
        clientSummeryAdapter = new ClientSummeryAdapter(producerSummeryModel.getClientSummeryModelArrayList(), getActivity(), this, true);
        recyclerView.setAdapter(clientSummeryAdapter);
    }

    private void attachListeners() {
        backImageButton.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void setData() {
        totalAmountTextView.setText(String.format("%s", producerSummeryModel.getTotalAmount()));
        titleTextView.setText(getFormattedDate(producerSummeryModel.getTimeStamp()));


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
        ClientSummeryModel clientSummeryModel = producerSummeryModel.getClientSummeryModelArrayList().get(position);
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
        dateTextView.setText(String.format(getFormattedDate(producerSummeryModel.getTimeStamp())));


        ClientSummeryItemDetailsAdapter clientSummeryItemDetailsAdapter = new ClientSummeryItemDetailsAdapter(clientSummeryModel.getAcquiredGoodModelArrayList(), getActivity());
        recyclerView.setAdapter(clientSummeryItemDetailsAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("ok", null);
        builder.setView(linearLayout);
        builder.show();
    }


    interface CallBack {
    }
}
