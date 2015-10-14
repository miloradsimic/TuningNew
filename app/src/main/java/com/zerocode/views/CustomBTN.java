package com.zerocode.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.zerocode.TuningNew;

/**
 * Created by tbojan on 1.9.2015.
 */
public class CustomBTN extends Button{
    public CustomBTN(Context context) {
        super(context);
        init();
    }

    public CustomBTN(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomBTN(Context context, AttributeSet attrs, int defStyleAttr) {
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
