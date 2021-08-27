package com.association.punchclock;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.association.punchclock.Models.Clock;
import com.association.punchclock.Models.CurLocation;
import com.association.punchclock.Models.User;

public class MainApplication extends Application {
    public static String Association_ID = null;
    public static User user = new User();
    public static CurLocation cur_location = null;
    public static SharedPreferences sharedPref = null;
    public static String WEATHERAPIKEY = "03456f99043143f8b93ca2e99c7dc67c";
    public static Clock selectedClock;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
    }
}
