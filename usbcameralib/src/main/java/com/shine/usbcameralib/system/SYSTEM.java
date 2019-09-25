package com.shine.usbcameralib.system;

import android.app.Activity;

import com.shine.usbcameralib.serialdog.serialutil.SystemManager;
import com.shine.usbcameralib.system.light.LightInterface;
import com.shine.usbcameralib.system.light.LightManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * 系统控制
 *
 * @author GP
 * @date 2016/8/18.
 */
public enum SYSTEM implements LightInterface {

    INSTANCE;

    public Activity activity;
    private LightManager lightManager;


    public void init(Activity activity) {
        this.activity = activity;
        lightManager = new LightManager(activity);
    }


    public void rootCommand(final String string) {
        new Thread() {
            @Override
            public void run() {
                try {
                    (new SystemManager()).RootCommand(string);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private Disposable keyLightOn;

    @Override
    public void doLightOn() {
        if (INSTANCE.lightManager != null) {
            INSTANCE.lightManager.doLightOn();
        }
        if (keyLightOn != null && !keyLightOn.isDisposed()) {
            keyLightOn.dispose();
        }
        keyLightOn = Flowable.just("")
                .subscribeOn(Schedulers.io())
                .map(new Function<String, Object>() {
                    @Override
                    public Object apply(String s) throws Exception {
                         Util.INSTANCE.openLight();
                        return s;
                    }
                })
                .delay(30, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                         Util.INSTANCE.closeLight();
                    }
                });

    }

    @Override
    public void doLightAllOn() {
        if (INSTANCE.lightManager != null) {

            INSTANCE.lightManager.doLightAllOn();
        }
    }

    @Override
    public void setLight(int light) {
        if (INSTANCE.lightManager != null) {
            INSTANCE.lightManager.setLight(light);
        }
    }

    @Override
    public void reLight() {
        if (INSTANCE.lightManager != null) {
            INSTANCE.lightManager.setNowLight();
        }
    }

    @Override
    public int getLight() {
        if (INSTANCE.lightManager != null) {
            return INSTANCE.lightManager.getLight();
        } else {
            return 60;
        }
    }

}
