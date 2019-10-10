package com.android.example.gawala.Producer.Utils;

import android.text.TextUtils;
import android.util.Log;

import com.android.example.gawala.Producer.Models.DistanceMatrixModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public final class HttpRequestHelper {
    private static final String LOG_TAG = "HTTPRequest";

    private HttpRequestHelper() {
    }


    public static String requestJsonData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }


        return jsonResponse;

    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 304) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + url);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    public static DistanceMatrixModel parseDistanceMatrixJson(String jsonString) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        DistanceMatrixModel distanceMatrixModel = null;

        try {
            JSONObject rootJsonObject = new JSONObject(jsonString);
            String rootResponseStatus = rootJsonObject.getString("status");

            if (!rootResponseStatus.equals("OK")) {
                return null;
            }

            String originAddress = rootJsonObject.getJSONArray("origin_addresses").getString(0);
            String destinationAddress = rootJsonObject.getJSONArray("origin_addresses").getString(0);

            JSONArray rowsArray = rootJsonObject.getJSONArray("rows");
            JSONObject elementsContainerObject = rowsArray.getJSONObject(0);
            JSONArray elementsArray = elementsContainerObject.getJSONArray("elements");


            JSONObject elementZeroObject = elementsArray.getJSONObject(0);
            String elementStatus = elementZeroObject.getString("status");
            if (!elementStatus.equals("OK")) {
                return null;
            }
            JSONObject distanceObject = elementZeroObject.getJSONObject("distance");
            String distanceText = distanceObject.getString("text");
            long distanceValue = distanceObject.getLong("value");

            JSONObject durationObject = elementZeroObject.getJSONObject("duration");
            String durationText = durationObject.getString("text");
            long durationValue = durationObject.getLong("value");

            distanceMatrixModel = new DistanceMatrixModel(originAddress, destinationAddress, distanceText, durationText, distanceValue, durationValue);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return distanceMatrixModel;
    }


}
