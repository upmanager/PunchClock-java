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
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.association.punchclock.MainApplication;
import com.association.punchclock.Models.Association;
import com.association.punchclock.Models.User;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
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
    public static String SHARED_ASSOCIATIONID = "ASSOCIATION";
    public static String SHARED_DEVICEINFOID = "DEVICEINFO";
    public static void saveToken() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHARED_TOKEN, MainApplication.user.getToken());
        editor.putInt(SHARED_ASSOCIATIONID, MainApplication.association.getId());
        editor.putInt(SHARED_DEVICEINFOID, MainApplication.mDeviceInfo.getId());
        editor.apply();
    }
    public static void getToken() {
        String token = sharedPref.getString(SHARED_TOKEN, null);
        int associationid = sharedPref.getInt(SHARED_ASSOCIATIONID, 0);
        int deviceinfoid = sharedPref.getInt(SHARED_DEVICEINFOID, 0);
        MainApplication.user.setToken(token);
        MainApplication.mDeviceInfo.setId(deviceinfoid);
        MainApplication.association.setId(associationid);
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


//    get device ip ipv4 -----------------------------------------------

    public static String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        if (isIPv4) return sAddr;
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } else {
                TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelephony.getDeviceId() != null) deviceId = mTelephony.getDeviceId();
                else
                    deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        if (deviceId == null || deviceId.isEmpty()) {
            try {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception err) {
                err.printStackTrace();
                deviceId = "Unknow device";
            }
        }

        return deviceId;
    }
    public static void initAuthData (JSONObject data, String token) {

        // -------------- get user ------------------------------------------
        User user = new User();
        try {
            JSONObject user_data = data.getJSONObject("user");
            int id = user_data.getInt("id");
            String username = user_data.getString("username");

            user.setUsername(username);
            user.setId(id);
            user.setToken(token);
        }catch (Exception err){
            err.printStackTrace();
        }
        MainApplication.user = user;


        // -------------------- get association ---------------------------------
        Association asso = new Association();
        try{
            JSONObject association_data = data.getJSONObject("association");
            int association_id = association_data.getInt("id");
            String address1 = association_data.getString("address1");
            String address2 = association_data.getString("address2");
            String city = association_data.getString("city");
            String country = association_data.getString("country");
            String name = association_data.getString("name");
            String pincode = association_data.getString("pincode");
            asso.setId(association_id);
            asso.setAddress1(address1);
            asso.setCity(city);
            asso.setCountry(country);
            asso.setAddress2(address2);
            asso.setName(name);
            asso.setPincode(pincode);
        }catch (Exception err){

        }
        MainApplication.association = asso;

        // -------------------- get device info ---------------------------------
        try {
            JSONObject device_info = data.getJSONObject("device_info");
            int device_id = device_info.getInt("id");
            MainApplication.mDeviceInfo.setId(device_id);
            int active = device_info.getInt("active");
            MainApplication.mDeviceInfo.setActive(active == 1);
        }catch (Exception err){
            err.printStackTrace();
        }
    }

}
