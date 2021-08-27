package com.association.punchclock.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.association.punchclock.R;

public class KeyboardView extends FrameLayout implements View.OnClickListener {
    public static int KEY_CLEAR = 10;
    public static int KEY_BACK = 11;

    public interface KeyboardListener {
        public void OnKeyboardPress(int keycode);
    }

    private KeyboardListener listener;

    public void SetKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }

    public KeyboardView(Context context) {
        super(context);
        init();
        this.listener = null;
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.keyboard, this);
        initViews();
    }

    private void initViews() {
        $(R.id.t9_key_0).setOnClickListener(this);
        $(R.id.t9_key_1).setOnClickListener(this);
        $(R.id.t9_key_2).setOnClickListener(this);
        $(R.id.t9_key_3).setOnClickListener(this);
        $(R.id.t9_key_4).setOnClickListener(this);
        $(R.id.t9_key_5).setOnClickListener(this);
        $(R.id.t9_key_6).setOnClickListener(this);
        $(R.id.t9_key_7).setOnClickListener(this);
        $(R.id.t9_key_8).setOnClickListener(this);
        $(R.id.t9_key_9).setOnClickListener(this);
        $(R.id.t9_key_clear).setOnClickListener(this);
        $(R.id.t9_key_backspace).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null && "number_button".equals(v.getTag())) {
            String tag = ((TextView) v).getText().toString();
            int i = Integer.parseInt(tag);
            listener.OnKeyboardPress(i);
            return;
        }
        switch (v.getId()) {
            case R.id.t9_key_clear:
                listener.OnKeyboardPress(KEY_CLEAR);
                break;
            case R.id.t9_key_backspace: // handle backspace button
                listener.OnKeyboardPress(KEY_BACK);
                break;
        }
    }

    protected <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }
}
