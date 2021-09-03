package com.association.punchclock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        try {
            int permissionCheckREAD_PHONE_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (permissionCheckREAD_PHONE_STATE != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 8);
                return;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        goNext();
    }

    private void goNext() {
        ApiService.checkAuth(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        goNext();
    }

    private void accessFailed() {
        MainApplication.user = new User();
        Utils.saveToken();
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

}