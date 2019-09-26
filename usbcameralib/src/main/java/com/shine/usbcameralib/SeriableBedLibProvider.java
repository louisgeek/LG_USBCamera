package com.shine.usbcameralib;

import android.content.Context;

/**
 * Created by louisgeek on 2018/9/20.
 */
public class SeriableBedLibProvider {
    private static Context mAppContext;
    public static void init(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static Context provideAppContext() {
        return mAppContext;
    }


}
