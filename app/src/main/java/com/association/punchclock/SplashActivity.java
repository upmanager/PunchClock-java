package com.association.punchclock;

import android.content.Intent;
import android.os.Bundle;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.association.punchclock.Models.User;
import com.association.punchclock.Utils.ApiService;
import com.association.punchclock.Utils.Utils;

import org.json.JSONObject;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Utils.getToken();
        ApiService.checkAuth(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    boolean success = response.getBoolean("success");
                    if(success){
                        JSONObject data = response.getJSONObject("data");
                        Utils.initAuthData(data, MainApplication.user.getToken());

                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                        return;
                    }
                }catch (Exception err){
                    err.printStackTrace();
                }
                accessFailed();
            }

            @Override
            public void onError(ANError anError) {
                accessFailed();
            }
        });
    }
    private void accessFailed(){
        MainApplication.user = new User();
        Utils.saveToken();
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

}