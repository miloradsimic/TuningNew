package com.zerocode;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by tbojan on 1.9.2015.
 */
public class TuningNew extends Application {


    public static Typeface CUSTOM_FONT;

    @Override
    public void onCreate() {
        super.onCreate();
        CUSTOM_FONT = Typeface.createFromAsset(this.getAssets(), "chickweed_titling.ttf");
    }

}
