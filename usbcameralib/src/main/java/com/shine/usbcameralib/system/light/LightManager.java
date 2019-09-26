package com.shine.usbcameralib.system.light;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import com.shine.usbcameralib.serialdog.serialutil.LogPlus;
import com.shine.usbcameralib.system.Util;
import com.shine.utilitylib.A64Utility;


/**
 * 灯光控制
 *
 * @author GP
 * @date 2016/8/18.
 */
public class LightManager {

    private Activity activity;
    private A64Utility mA64Utility;

    public LightManager(Activity activity) {
        this.activity = activity;
        mA64Utility = new A64Utility();
    }

    private Handler handler = new Handler();
    /**当前亮度*/
    private int light = 100;
    private int nowLight = -1;
    private final int lightLimit = 35;

    /**点击后亮10秒后关闭灯光*/
    public void doLightOn() {
        handler.removeCallbacks(lightRunnable);
        LogPlus.i("doLightOn light:" + light + "===" + lightLimit);
        if (light < lightLimit) {
            if (mA64Utility != null) {
                mA64Utility.OpenScreen();
            }
            if (nowLight != 58) {
                nowLight = 58;
                saveScreenBrightness(150);
                setScreenBrightness(150);
            }
            handler.postDelayed(lightRunnable, Util.INSTANCE.lightSleep*1000);
        }
    }

    // 点击后亮
    public void doLightAllOn() {
        handler.removeCallbacks(lightRunnable);
        LogPlus.i("light:" + light  + "====" + lightLimit);
        if (light < lightLimit) {
            if (mA64Utility != null) {
                mA64Utility.OpenScreen();
            }
            if (nowLight != 58) {
                nowLight = 58;
                saveScreenBrightness(150);
                setScreenBrightness(150);
            }
        }
    }

    /**设置灯光*/
    public void setLight(int light) {

        LogPlus.i("nowLight:" + nowLight + "----light:" + light);
        if (nowLight == light) {
            return;
        }

        this.light = light;
        nowLight = light;
        if (light == 0) {
            this.light = 0;
            nowLight = 1;
            // nowLight = 0;
            if (mA64Utility != null) {
                mA64Utility.CloseScreen();
            }
        } else {
            if (mA64Utility != null) {
                mA64Utility.OpenScreen();
            }
        }

        saveScreenBrightness((int) (Float.parseFloat(String.valueOf(nowLight)) / 100 * 255));
        setScreenBrightness((int) (Float.parseFloat(String.valueOf(nowLight)) / 100 * 255));

        final int finalLight = light;
        Util.INSTANCE.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = activity
                        .getSharedPreferences("nurseBedInfo",
                                Context.MODE_PRIVATE).edit();
                editor.putInt("light", finalLight);
                editor.apply();
            }
        });
    }

    // 恢复当前亮度
    public void setNowLight() {
        handler.removeCallbacks(lightRunnable);
        handler.postDelayed(lightRunnable, 1000);
    }

    // 停止灯光
    private Runnable lightRunnable = new Runnable() {
        @Override
        public void run() {
            LogPlus.i("lightRunnable ----- light:" + light);
            setLight(light);
        }
    };

    /**
     * 设置当前屏幕亮度值 0--255
     */
    private void saveScreenBrightness(int paramInt){
        paramInt = Math.abs(paramInt - 255);
        if (paramInt <= 1) {
            paramInt = 1;
        }
        try{
            Settings.System.putInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, paramInt);
        }
        catch (Exception localException){
            localException.printStackTrace();
        }
    }

    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    private void setScreenBrightness(int paramInt) {
        paramInt = Math.abs(paramInt - 255);
        if (paramInt <= 1) {
            paramInt = 1;
        }

        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        localLayoutParams.screenBrightness = paramInt / 255.0F;
        localWindow.setAttributes(localLayoutParams);
    }

    public int getLight() {
        return light;
    }
}