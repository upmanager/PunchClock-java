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
        ApiService.checkAuth(MainApplication.user.getToken(), new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    MainApplication.user.setId(response.getInt("id"));
                    MainApplication.user.setEmail(response.getString("email"));
                }catch (Exception err){
                    err.printStackTrace();
                }
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(ANError anError) {
                Utils.saveToken("", "");
                MainApplication.user = new User();
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

}