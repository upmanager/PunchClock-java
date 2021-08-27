package com.association.punchclock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.association.punchclock.Models.User;
import com.association.punchclock.Utils.ApiService;
import com.association.punchclock.Utils.Utils;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.edt_association_id)
    TextInputEditText edt_association_id;
    @BindView(R.id.edt_email)
    TextInputEditText edt_email;
    @BindView(R.id.edt_password)
    TextInputEditText edt_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
//        edt_association_id.setText("test");
//        edt_email.setText("info@building.com");
//        edt_password.setText("12345678");
    }

    private void login() {
        String email = edt_email.getText().toString();
        String password = edt_password.getText().toString();
        String association_id = edt_association_id.getText().toString();
        if (email.equals("") || password.equals("") || association_id.equals("")) {
            showErrorDialog("Please input valid info!");
            return;
        }
        showLoading();
        ApiService.login(email, password, new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hideLoading();
                    Boolean success = response.getBoolean("success");
                    if (success) {
                        JSONObject data = response.getJSONObject("data");
                        JSONObject user_data = data.getJSONObject("user");
                        int id = user_data.getInt("id");
                        String token = data.getString("token");
                        Utils.saveToken(token, association_id);
                        User user = new User();
                        user.setEmail(email);
                        user.setId(id);
                        user.setToken(token);
                        user.setAssociation(association_id);
                        MainApplication.user = user;
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        return;
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
                showErrorDialog("Login failed, please input valid info!");
            }

            @Override
            public void onError(ANError anError) {
                hideLoading();
                showErrorDialog("Login failed, please input valid info!");
            }
        });
    }

    @OnClick({R.id.btn_login})
    protected void onVewClick(View v) {
        if (v.getId() == R.id.btn_login) {
            login();
        }
    }
}