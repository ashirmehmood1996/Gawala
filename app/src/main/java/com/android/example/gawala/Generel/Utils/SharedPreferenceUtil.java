package com.android.example.gawala.Generel.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferenceUtil {
    public static final String GAWALA_PREF = "GawalaPrefrences";

    private SharedPreferenceUtil() { //making private so that in future any contributor can not make an object
    }

    public static void storeValue(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GAWALA_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).commit();
    }

    public static String getValue(Context context, String key) {
        return context.getSharedPreferences(GAWALA_PREF, Context.MODE_PRIVATE).getString(key, null);
    }

    public static boolean hasValue(Context context, String key) {
        return context.getSharedPreferences(GAWALA_PREF, Context.MODE_PRIVATE).contains(key);


    }
    public static void clearAllPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GAWALA_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();//comit will make changes immidiately and can hang the UI thread if theres is lots of data while apply will take the operation to another thread

    }

}
