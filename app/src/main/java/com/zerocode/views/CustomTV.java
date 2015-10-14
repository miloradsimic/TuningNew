package com.zerocode.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zerocode.TuningNew;

/**
 * Created by tbojan on 1.9.2015.
 */
public class CustomTV extends TextView {

    public CustomTV(Context context) {
        super(context);
        init();
    }

    public CustomTV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        int style = Typeface.NORMAL;

        if (getTypeface() != null) {
            style = getTypeface().getStyle();
        }

        setTypeface(TuningNew.CUSTOM_FONT, style);
    }
}
