package com.android.example.gawala.Generel.Fraagments;


import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.example.gawala.Generel.Utils.SharedPreferenceUtil;
import com.android.example.gawala.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.Arrays;
import java.util.List;


public class DistanceViewerMapsFragment extends DialogFragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String OTHERS_TITLE = "othersTitle";


    // TODO: Rename and change types of parameters
//    private RequestModel mRequestModel;

//    private CallBack mListener;


    private GoogleMap mMap;
    private ImageButton backImageButton;
    private double lat;
    private double lng;
    private String othersTitle;


    public DistanceViewerMapsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param lat latitude of other party
     * @param lng longitude of other party
     * @return A new instance of fragment DistanceViewerMapsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DistanceViewerMapsFragment newInstance(Double lat, Double lng, String othersTitle) {
        DistanceViewerMapsFragment fragment = new DistanceViewerMapsFragment();

        Bundle args = new Bundle();
        args.putDouble(LAT, lat);
        args.putDouble(LNG, lng);
        args.putString(OTHERS_TITLE, othersTitle);
//        args.putSerializable(REQUEST_MODEL, requestModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble(LAT);
            lng = getArguments().getDouble(LNG);
            othersTitle = getArguments().getString(OTHERS_TITLE);
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreendialogTheme);

    }

    //this link helped me out also the comment informed me that we should commitAllowingsatteloss to avoid crash when activty is recreated
    //https://stackoverflow.com/a/14484640/6039129
    @Override
    public void onDestroy() {
        super.onDestroy();

        Fragment mapFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frag_distance_viewer_map);

        if (mapFragment != null)
            getFragmentManager().beginTransaction().remove(mapFragment).commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_distance_viewer_maps, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.frag_distance_viewer_map);
        mapFragment.getMapAsync(this);
        backImageButton = rootView.findViewById(R.id.ib_frag_distance_viewer_back);
        backImageButton.setOnClickListener(v -> {
            dismiss();
        });
        return rootView;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        LatLng clientLatlng = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(clientLatlng).title(othersTitle));

        String latStr = SharedPreferenceUtil.getValue(getActivity().getApplicationContext(), "lat");
        String lngStr = SharedPreferenceUtil.getValue(getActivity().getApplicationContext(), "lng");
        if (!(latStr != null && !latStr.isEmpty() && lngStr != null && !lngStr.isEmpty())) {
            Toast.makeText(getActivity(), "problem in getting location from shared preferences", Toast.LENGTH_SHORT).show();
            return;
        }
        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);
        LatLng myLatLng = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("You"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLatLng).include(clientLatlng);


        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        showCurvedPolyline(myLatLng, clientLatlng, .1);
    }

    //method taken from the following link
    //https://stackoverflow.com/a/43665433/6039129
    private void showCurvedPolyline(LatLng p1, LatLng p2, double k) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1, p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        //Midpoint position
        LatLng p = SphericalUtil.computeOffset(p1, d * 0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - k * k) * d * 0.5 / (2 * k);
        double r = (1 + k * k) * d * 0.5 / (2 * k);

        LatLng c = SphericalUtil.computeOffset(p, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, p1);
        double h2 = SphericalUtil.computeHeading(c, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 100;
        double step = (h2 - h1) / numpoints;

        for (int i = 0; i < numpoints; i++) {
            LatLng pi = SphericalUtil.computeOffset(c, r, h1 + i * step);
            options.add(pi);
        }

        //Draw polyline
        Polyline polyline = mMap.addPolyline(options.width(10).color(Color.BLACK).geodesic(false).pattern(pattern));
        polyline.setTag("this is polyline");
    }
}
