package com.android.example.gawala.Producer.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.Producer.Models.DistanceMatrixModel;
import com.android.example.gawala.Producer.Models.RequestModel;
import com.android.example.gawala.Producer.Utils.DistanceMatrixAsyncTask;
import com.android.example.gawala.Producer.Utils.HttpRequestHelper;
import com.android.example.gawala.Producer.Utils.UrlGenrator;
import com.android.example.gawala.R;
import com.google.android.gms.maps.model.LatLng;

public class ClientInfoFragment extends DialogFragment implements View.OnClickListener {

    private static final String REQUEST_MODEL = "requestModel";
    private static final String TAG = "ClientDialogFragment";

    private Callbacks callbacks;
    private RequestModel requestModel;


    private TextView nameTextView, numberTextView, locationTextView, typeTextView;
    private ImageButton backImageButton;
    private Button showLocationOnMapButton;


    public static ClientInfoFragment getInstance(RequestModel requestModel) {
        ClientInfoFragment fragment = new ClientInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(REQUEST_MODEL, requestModel);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setCallback(Callbacks callbacks) {
        this.callbacks = callbacks;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);

        requestModel = (RequestModel) getArguments().getSerializable(REQUEST_MODEL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_client_info_deltails_full_screen, container, false);

        initFields(rootView);
        calculateDistance();
        attachListeners();
        setData();
        return rootView;
    }


    private void initFields(View rootView) {

        nameTextView = rootView.findViewById(R.id.tv_frag_con_details_name);
        numberTextView = rootView.findViewById(R.id.tv_frag_con_details_number);
        locationTextView = rootView.findViewById(R.id.tv_frag_con_details_location);
        typeTextView = rootView.findViewById(R.id.tv_frag_con_details_user_type);

        backImageButton = rootView.findViewById(R.id.ib_frag_con_details_back);
        showLocationOnMapButton = rootView.findViewById(R.id.bt_frag_con_details_distance_desc);
    }

    private void attachListeners() {
        backImageButton.setOnClickListener(this);
        showLocationOnMapButton.setOnClickListener(this);


    }

    private void setData() {
        nameTextView.setText(requestModel.getName());
        numberTextView.setText(requestModel.getNumber());
        typeTextView.setText("Consumer");
        locationTextView.setText(requestModel.getAddress());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_frag_con_details_back:
                dismiss();
                break;
            case R.id.bt_frag_con_details_distance_desc:
                showMap();
                break;
        }
    }

    private void showMap() {
        Toast.makeText(getActivity(), "implement this ", Toast.LENGTH_SHORT).show();
        // TODO: 11/17/2019  show map in a dialog fragment similar to this one


        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        DistanceViewerMapsFragment clientInfoFullScreenDialogFragment = (DistanceViewerMapsFragment) getActivity().getSupportFragmentManager().findFragmentByTag("a");
        if (clientInfoFullScreenDialogFragment != null) {
            fragmentTransaction.remove(clientInfoFullScreenDialogFragment).commit();
        }
        DistanceViewerMapsFragment dialogFragment = DistanceViewerMapsFragment.newInstance(requestModel);
//        dialogFragment.setCallback(this);
        dialogFragment.show(fragmentTransaction, "a");
    }

    private void calculateDistance() {
        String latStr = SharedPreferenceUtil.getValue(getActivity().getApplicationContext(), "lat");
        String lngStr = SharedPreferenceUtil.getValue(getActivity().getApplicationContext(), "lng");
        if (!(latStr != null && !latStr.isEmpty() && lngStr != null && !lngStr.isEmpty())) {
            Toast.makeText(getActivity(), "problem in getting location from shared preferences", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "did not fetch the location stored in shared preferences");
            showLocationOnMapButton.setText("unable to fetch the location");

            return;
        }
        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);
        LatLng myLatLng = new LatLng(lat, lng);

        LatLng clientLatLng = new LatLng(Double.parseDouble(requestModel.getLat()), Double.parseDouble(requestModel.getLng()));

        String url = UrlGenrator.generateDistanceMatrixUrl(myLatLng, clientLatLng, getResources().getString(R.string.distance_matrix_api_key));
        new DistanceMatrixAsyncTask(){
            @Override
            protected void onPostExecute(String s) {

                DistanceMatrixModel distanceMatrixModel = HttpRequestHelper.parseDistanceMatrixJson(s);

               setButtonText(distanceMatrixModel.getDistance());
            }
        }.execute(url);

    }

    private void setButtonText(String text){
        showLocationOnMapButton.setEnabled(true);
        showLocationOnMapButton.setText(String.format("This client is about %s by road distance from your Location, Tap here to see on map",text));
    }

    public interface Callbacks {

    }
}
