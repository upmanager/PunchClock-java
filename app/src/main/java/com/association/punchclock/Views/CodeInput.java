package com.association.punchclock.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.association.punchclock.R;

import java.util.ArrayList;

public class CodeInput extends FrameLayout implements View.OnClickListener {
    private ArrayList<TextView> mCodeView = new ArrayList<>();
    private int currentIndex = -1;


    public interface CompleteListener {
        public void OnComplete(String key);
    }

    private CompleteListener listener;

    public void SetOnCompleteListener(CompleteListener listener) {
        this.listener = listener;
    }

    public CodeInput(Context context) {
        super(context);
        init();
    }

    public CodeInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.listener = null;
        inflate(getContext(), R.layout.code_input, this);
        initViews();
    }

    private void initViews() {
        mCodeView.add($(R.id.txt_code_1));
        mCodeView.add($(R.id.txt_code_2));
        mCodeView.add($(R.id.txt_code_3));
        mCodeView.add($(R.id.txt_code_4));
        mCodeView.add($(R.id.txt_code_5));
        mCodeView.add($(R.id.txt_code_6));
        for (int i = 0; i < mCodeView.size(); i++) {
            if (i == 0) {
                mCodeView.get(i).setBackgroundResource(R.drawable.active_shadow);
            } else {
                mCodeView.get(i).setBackgroundResource(R.drawable.shadow);
            }
            mCodeView.get(i).setOnClickListener(this);
        }
    }

    public void sendKey(int key) {
        if (currentIndex >= 0 && key == KeyboardView.KEY_BACK) {
            clear(currentIndex);
        } else if (key == KeyboardView.KEY_CLEAR) {
            clear(0);
        } else if (key != KeyboardView.KEY_BACK && currentIndex + 1 < mCodeView.size()) {
            currentIndex++;
            focus(currentIndex + 1);
            mCodeView.get(currentIndex).setText(String.valueOf(key));
            if (currentIndex + 1 == mCodeView.size()) {
                StringBuilder res_code = new StringBuilder();
                for (TextView txt : mCodeView) {
                    res_code.append(txt.getText().toString());
                }
                listener.OnComplete(res_code.toString());
            }
        }
    }

    private void focus(int index) {
        for (int i = 0; i < mCodeView.size(); i++) {
            if (i == index) {
                mCodeView.get(i).setBackgroundResource(R.drawable.active_shadow);
            } else {
                mCodeView.get(i).setBackgroundResource(R.drawable.shadow);
            }
        }
    }

    public void clear(int index) {
        focus(index);
        currentIndex = index - 1;
        for (int i = index; i < mCodeView.size(); i++) {
            mCodeView.get(i).setText("");
        }
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < mCodeView.size(); i++) {
            if (mCodeView.get(i).getId() == v.getId()) {
                if (currentIndex >= i) {
                    clear(i);
                }
                return;
            }
        }
    }

    protected <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }
}