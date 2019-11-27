package com.android.example.gawala.Producer.Utils;

import android.os.AsyncTask;

public class DistanceMatrixAsyncTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... strings) {

        String requestUrl = strings[0];
        return HttpRequestHelper.requestJsonData(requestUrl);

    }
}
