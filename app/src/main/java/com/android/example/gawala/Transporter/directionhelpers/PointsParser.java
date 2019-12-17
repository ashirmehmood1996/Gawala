package com.android.example.gawala.Transporter.directionhelpers;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * use of these helper classes is demonstrated in this youtube video https://www.youtube.com/watch?v=wRDLjUK8nyU
 * Created by Vishal on 10/20/2018. borrowed by ashir Mehmood on 08/15/2019 using link below
 * https://github.com/Vysh01/android-maps-directions
 */

public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    TaskLoadedCallback taskCallback;
    String directionMode = "driving";
    private boolean isfullRoute = false;

    public PointsParser(Context mContext, String directionMode, boolean isfullRoute) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.directionMode = directionMode;
        this.isfullRoute = isfullRoute;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            System.out.println("directions api jsonResponse : " + jsonData[0]);
            jObject = new JSONObject(jsonData[0]);
            Log.d("mylog", jsonData[0].toString());
            DataParser parser = new DataParser();
            Log.d("mylog", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            Log.d("mylog", "Executing routes");
            Log.d("mylog", routes.toString());

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        if (result == null) return;
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            if (directionMode.equalsIgnoreCase("walking")) {

                if (isfullRoute) {
                    lineOptions.width(20);
                    lineOptions.color(Color.GREEN);
                } else {
                    lineOptions.width(15);
                    lineOptions.color(Color.BLUE);
                }

            } else {

                if (isfullRoute) {
                    lineOptions.width(20);
                    lineOptions.color(Color.GREEN);
                } else {
                    lineOptions.width(15);
                    lineOptions.color(Color.BLUE);
                }
            }
            Log.d("mylog", "onPostExecute lineoptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            //mMap.addPolyline(lineOptions);
            taskCallback.onTaskDone(lineOptions, isfullRoute);

        } else {
            Log.d("mylog", "without Polylines drawn");
        }
    }
}
