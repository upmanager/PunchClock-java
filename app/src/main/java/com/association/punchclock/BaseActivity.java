package com.association.punchclock;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BaseActivity extends Activity {

    public SweetAlertDialog loadingDlg = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

    }

    public void showDialog(int type, String title, String content) {
        new SweetAlertDialog(this, type)
                .setTitleText(title)
                .setContentText(content)
                .show();
    }
    public void showCancelableDialog(int type, String title, String content, String cancelText, String confirmText, SweetAlertDialog.OnSweetClickListener confirmListener) {
        new SweetAlertDialog(this, type)
                .setTitleText(title)
                .setContentText(content)
                .setContentTextSize(20)
                .setCancelText(cancelText)
                .setConfirmText(confirmText)
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(confirmListener)
                .show();
    }
    public void showCancelableDialog(String title, String content, String confirmText, SweetAlertDialog.OnSweetClickListener confirmListener) {
        showCancelableDialog(SweetAlertDialog.NORMAL_TYPE, title, content, "Cancel", confirmText, confirmListener);
    }
    public void showSuccessDialog(String title, String content) {
        showDialog(SweetAlertDialog.SUCCESS_TYPE, title, content);
    }
    public void showSuccessDialog(String content){
        showSuccessDialog("Success", content);
    }
    public void showErrorDialog(String title, String content){
        showDialog(SweetAlertDialog.ERROR_TYPE, title, content);
    }
    public void showErrorDialog(String content){
        showErrorDialog("Oops...", content);
    }
    public void showLoading(String message) {
        loadingDlg = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        loadingDlg.setTitleText(message);
        loadingDlg.setCancelable(false);
        loadingDlg.show();
        loadingDlg.setTitleText(message).show();
    }
    public void showLoading(){
        showLoading("Loading");
    }
    public void hideLoading(){
        if(loadingDlg != null){
            loadingDlg.dismissWithAnimation();
            loadingDlg.cancel();
        }
    }
}