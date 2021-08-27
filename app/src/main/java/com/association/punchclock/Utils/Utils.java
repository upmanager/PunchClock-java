package com.association.punchclock.Utils;

import static com.association.punchclock.MainApplication.sharedPref;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.association.punchclock.MainApplication;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Utils {
    public static boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
    public static String SHARED_TOKEN = "TOKEN";
    public static String SHARED_ASSOCIATION = "ASSOCIATION";
    public static void saveToken(String token, String associationId) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_TOKEN, token);
        editor.putString(SHARED_ASSOCIATION, associationId);
        editor.apply();
    }
    public static void getToken() {
        String token = sharedPref.getString(SHARED_TOKEN, null);
        String association = sharedPref.getString(SHARED_ASSOCIATION, null);
        MainApplication.user.setAssociation(association);
        MainApplication.user.setToken(token);
    }
    @SuppressLint("SimpleDateFormat")
    public static String parseDate(Date date){
        String date_res = "";
        int day_duration =  (new Date()).getDay() - date.getDay();
        if(day_duration == 0){
            date_res = "Today ";
        } else if(day_duration == 1) {
            date_res = "Yesterday ";
        }else {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            date_res = format.format(date);
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        date_res += format.format(date);

        return date_res;
    }
    @NonNull
    @SuppressLint("SimpleDateFormat")
    public static String parseDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return parseDate(format.parse(date));
        }catch (Exception err){
            err.printStackTrace();
            return "";
        }
    }

    public static Matrix configureTransform(int viewWidth, int viewHeight, Size mPreviewSize, Activity context) {

        int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        return matrix;
    }

    public static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {

            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                         int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Utils.CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new Utils.CompareSizesByArea());
        } else {
            Log.e("tag", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

}
