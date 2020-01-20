package com.android.example.gawala.Generel.Utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.widget.RatingBar;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.android.example.gawala.R;

public final class RatingBarUtils {
    private RatingBarUtils() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setAppropriatecolor(RatingBar ratingBar, float rating) {
        if (rating == 0.0) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.RED));


        } else if (rating == 1.0) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));

        } else if (rating == 0.5) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 1.5) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 2.0) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 2.5) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 3.0) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 3.5) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 4.0) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 4.5) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        } else if (rating == 5.0) {
            ratingBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));


        }
    }
}
