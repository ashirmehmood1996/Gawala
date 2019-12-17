package com.android.example.gawala.Generel.AsyncTasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * this class serves to get location  in String form
 */
public class GeoCoderAsyncTask extends AsyncTask<LatLng, Void, Address> {
    private Context context;

    public GeoCoderAsyncTask(Context context) {
        this.context = context;
    }

    /**
     * @param latLngs
     * @return
     */
    @Override
    protected Address doInBackground(LatLng... latLngs) {
        Address address = null;
        try {
            double latitude = latLngs[0].latitude;
            double longitude = latLngs[0].longitude;

            System.out.println("lat : " + latitude + "lon : " + longitude);
            assert context != null;
            Geocoder geo = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            } else {
                if (addresses.size() > 0) {
                    address = addresses.get(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;

    }

}