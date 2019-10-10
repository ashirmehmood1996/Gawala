package com.android.example.gawala.Producer.Utils;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public final class UrlGenrator {
    private UrlGenrator() {
    }


    public static String generateDistanceMatrixUrl(LatLng startPoint, LatLng endPoint, String key) {

        String resultantUrl = null;
        if (startPoint == null || endPoint == null) return null;

        Uri uri = Uri.parse("https://maps.googleapis.com/maps/api/distancematrix/");
        Uri.Builder uriBuilder = uri.buildUpon();
        uriBuilder.appendPath("json");//format in  which the result is needed
        uriBuilder.appendQueryParameter("origins", startPoint.latitude + "," + startPoint.longitude);
        uriBuilder.appendQueryParameter("destinations", endPoint.latitude + "," + endPoint.longitude);
        uriBuilder.appendQueryParameter("key", key);
        resultantUrl = uriBuilder.toString();

        return resultantUrl;

    }
}
