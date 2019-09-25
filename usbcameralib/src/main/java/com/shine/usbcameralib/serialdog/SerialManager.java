package com.shine.usbcameralib.serialdog;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.shine.usbcameralib.serialdog.thread.ARMThread;
import com.shine.usbcameralib.serialdog.thread.SerialThread;

import java.lang.ref.WeakReference;

/**
 * 串口管理类
 *
 * @author GP
 * @date 2016/8/31.
 */
public class SerialManager {

    public static Activity activity;
    private SerialThread serialThread;
//    private String serialPath = "/dev/ttyS3";
    private String serialPath = "/dev/ttyS4";
    private int baudrate = 9600;

    public final static int RECEIVE_STARTCHECK = 1;
    public final static int SERIALDISCONNECT = 2;

    public SerialHandler serialHandler;
    private ARMSerial armSerial;

    public SerialManager() {
        serialHandler = new SerialHandler(this);
        armSerial = new A64SerialImpl(this);
        runSerialThread(serialHandler, armSerial);
    }

    void runSerialThread(Handler handler, ARMSerial armSerial) {
        serialThread = new ARMThread(handler, this.serialPath, this.baudrate, armSerial);
        serialThread.start();
    }

    public static class SerialHandler extends Handler {

        private WeakReference<SerialManager> serialManagerWeakReference;

        SerialHandler(SerialManager serialManager) {
            serialManagerWeakReference = new WeakReference<SerialManager>(serialManager);
        }

        @Override
        public void handleMessage(Message msg) {
            SerialManager serialManager = serialManagerWeakReference.get();
            if (serialManager == null) {
                return;
            }
            switch (msg.what) {
                case RECEIVE_STARTCHECK:
                    serialManager.serialHandler.removeCallbacks(serialManager.restartSerial);
                    serialManager.serialHandler.postDelayed(serialManager.restartSerial, 1000 * 45);
                    break;
                case SERIALDISCONNECT:
                    serialManager.runSerialThread(serialManager.serialHandler, serialManager.armSerial);
                    break;
            }
        }
    }

    public Runnable restartSerial = new Runnable() {
        @Override
        public void run() {
            if (serialThread != null) {
                serialThread.onDestroy();
                runSerialThread(serialHandler, armSerial);
            }
        }
    };

    public void stop() {
        serialHandler.removeCallbacks(restartSerial);
        if (serialThread != null) {
            serialThread.onDestroy();
        }
    }

    public void openLight() {
        if (serialThread != null) {
            serialThread.openLight();
        }
    }

    public void closeLight() {
        if (serialThread != null) {
            serialThread.closeLight();
        }
    }

    public void stopSerial() {
        serialHandler.removeCallbacks(restartSerial);
        if (serialThread != null) {
            serialThread.onDestroy();
        }
    }

    public void stopDog() {
        if (serialThread != null) {
            serialThread.closeDog();
        }
    }
}
