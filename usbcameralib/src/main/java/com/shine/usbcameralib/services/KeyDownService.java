package com.shine.usbcameralib.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.shine.usbcameralib.serialdog.bean.KeyUp;
import com.shine.usbcameralib.serialdog.serialutil.LogPlus;
import com.shine.usbcameralib.serialdog.serialutil.RxBus;


/**
 * @author noxingde@163.com
 * @time 2019/7/31 11:12
 * @description:
 */
public class KeyDownService extends Service {

    public IntentFilter intentFilter = new IntentFilter("com.android.server.PhoneWindowManager.action.EXTKEYEVENT");

    @Override
    public void onCreate() {
        super.onCreate();
        LogPlus.i("start KeyDownService");
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int scanCode = intent.getIntExtra("scanCode", 0);
            KeyUp keyUp = new KeyUp();
            LogPlus.i("接收到广播按键Key:" + scanCode);
            switch (scanCode) {
                case 190:
                    keyUp.keyCode = 137;
                    break;
                case 191:
                    keyUp.keyCode = 138;
                    break;
                case 192:
                    keyUp.keyCode = 139;
                    break;
                case 193:
                    keyUp.keyCode = 140;
                    break;
                case 139:
                    keyUp.keyCode = 82;
                    break;
            }
            keyUp.keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyUp.keyCode);
            LogPlus.i("post keyUp:" + keyUp.keyCode);
            RxBus.INSTANCE.post(keyUp);

        }
    };
}

