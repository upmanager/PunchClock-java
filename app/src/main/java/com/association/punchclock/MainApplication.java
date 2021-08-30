package com.association.punchclock;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.association.punchclock.Models.Association;
import com.association.punchclock.Models.Clock;
import com.association.punchclock.Models.CurLocation;
import com.association.punchclock.Models.DeviceInfo;
import com.association.punchclock.Models.User;
import com.association.punchclock.Utils.Utils;

public class MainApplication extends Application {
    public static User user = new User();
    public static Association association = new Association();
    public static CurLocation cur_location = new CurLocation();
    public static SharedPreferences sharedPref = null;
    public static String WEATHERAPIKEY = "03456f99043143f8b93ca2e99c7dc67c";
    public static Clock selectedClock;
    public static DeviceInfo mDeviceInfo = new DeviceInfo();

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        String deviceId= Utils.getDeviceId(this);
        String ipAddress = Utils.getIPAddress();
        mDeviceInfo.setIpAddress(ipAddress);
        mDeviceInfo.setSerialNumber(deviceId);
    }
}
