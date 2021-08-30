package com.association.punchclock.Utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.association.punchclock.MainApplication;

import java.io.File;
import java.util.Date;

public final class ApiService {

    public interface API_URL {
//        String BASEURL = "http://192.168.107.160";
        String BASEURL = "https://total-association.com";
        String LOGIN = BASEURL + "/api/login";
        String HEALTH = BASEURL + "/api/health";
        String CLOCKSTATE = BASEURL + "/api/clock-state";
        String CLOCKUPDATE = BASEURL + "/api/clock-update";
        String UPLOADFILE = BASEURL + "/api/upload";
        String GETWEATHER = "https://api.weatherbit.io/v2.0/current";
    }

    public static void login(String email, String password, String association, JSONObjectRequestListener response){
        AndroidNetworking.post(API_URL.LOGIN)
                .addBodyParameter("email", email)
                .addBodyParameter("password", password)
                .addBodyParameter("association", association)
                .addBodyParameter("serial_number", MainApplication.mDeviceInfo.getSerialNumber())
                .addBodyParameter("ip_address", MainApplication.mDeviceInfo.getIpAddress())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }
    public static void checkAuth(JSONObjectRequestListener response){
        String token = MainApplication.user.getToken();
        int asoid = MainApplication.association.getId();
        AndroidNetworking.post(API_URL.HEALTH)
                .addHeaders("Authorization", "Bearer "+token)
                .addBodyParameter("association_id", String.valueOf(asoid))
                .addBodyParameter("serial_number", MainApplication.mDeviceInfo.getSerialNumber())
                .addBodyParameter("ip_address", MainApplication.mDeviceInfo.getIpAddress())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }
    public static void checkClockState(String clock_code, JSONObjectRequestListener response){
        AndroidNetworking.post(API_URL.CLOCKSTATE)
                .addHeaders("Authorization", "Bearer "+ MainApplication.user.getToken())
                .addBodyParameter("clock_code", clock_code)
                .addBodyParameter("association", String.valueOf(MainApplication.association.getId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }
    @SuppressLint("SimpleDateFormat")
    public static void updateClock(JSONObjectRequestListener response){
        if(MainApplication.selectedClock == null) return;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getDefault());
        String date = format.format(new Date());

        AndroidNetworking.post(API_URL.CLOCKUPDATE)
                .addHeaders("Authorization", "Bearer "+ MainApplication.user.getToken())
                .addBodyParameter("id", MainApplication.selectedClock.getStrId())
                .addBodyParameter("state", MainApplication.selectedClock.getStrState())
                .addBodyParameter("workerid", MainApplication.selectedClock.getStrWorkerid())
                .addBodyParameter("association", String.valueOf(MainApplication.association.getId()))
                .addBodyParameter("image", MainApplication.selectedClock.image)
                .addBodyParameter("date", date)
                .addBodyParameter("latitude", String.valueOf(MainApplication.cur_location.getLatitude()))
                .addBodyParameter("longitude", String.valueOf(MainApplication.cur_location.getLongitude()))
                .addBodyParameter("area", MainApplication.cur_location.getArea())
                .addBodyParameter("city", MainApplication.cur_location.getCity())
                .addBodyParameter("country", MainApplication.cur_location.getCountry())
                .addBodyParameter("postal_code", MainApplication.cur_location.getPostal_code())
                .addBodyParameter("deviceid", String.valueOf(MainApplication.mDeviceInfo.getId()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }
    public static void getWeather(JSONObjectRequestListener response) {
        AndroidNetworking.get(API_URL.GETWEATHER)
                .addQueryParameter("postal_code", String.valueOf(MainApplication.association.getPincode()))
                .addQueryParameter("key", MainApplication.WEATHERAPIKEY)
                .addHeaders("Accept", "application/json")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(response);
    }

    public static void getBitmap(String imageUrl, BitmapRequestListener bitmapRequestListener){
        AndroidNetworking.get(imageUrl)
                .setTag("imageRequestTag")
                .setPriority(Priority.MEDIUM)
                .setBitmapMaxHeight(100)
                .setBitmapMaxWidth(100)
                .setBitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
                .getAsBitmap(bitmapRequestListener);
    }
    public static void uploadFile(File file, JSONObjectRequestListener jsonObjectRequestListener) {
        uploadFile(file, new UploadProgressListener() {
            @Override
            public void onProgress(long bytesUploaded, long totalBytes) {

            }
        }, jsonObjectRequestListener);
    }
    public static void uploadFile(File file, UploadProgressListener uploadProgressListener, JSONObjectRequestListener jsonObjectRequestListener) {
        AndroidNetworking.upload(API_URL.UPLOADFILE)
                .addHeaders("Authorization", "Bearer "+ MainApplication.user.getToken())
                .addMultipartFile("image",file)
//                .addMultipartParameter("key","value")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(uploadProgressListener)
                .getAsJSONObject(jsonObjectRequestListener);

    }
}
