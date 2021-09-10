package com.association.punchclock.Views;

import android.content.Context;
import android.icu.text.DateFormat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.association.punchclock.R;

import java.util.Calendar;

public class CurrentDateTime extends LinearLayout {
    private TextView txt_cur_date;

    public CurrentDateTime(Context context) {
        super(context);
        init();
    }

    public CurrentDateTime(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurrentDateTime(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_current_date_time, this);
        txt_cur_date = findViewById(R.id.txt_cur_date);
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.DEFAULT).format(calendar.getTime());
        txt_cur_date.setText(currentDate);

    }
}
