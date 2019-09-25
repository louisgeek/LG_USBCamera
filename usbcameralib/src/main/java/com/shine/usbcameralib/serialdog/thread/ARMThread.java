/**
 * com.bed.nurse.shine.thread
 *
 * @version 1.0.0
 * <p>
 * Copyright (C) 2015-2015 神州视瀚.
 */
package com.shine.usbcameralib.serialdog.thread;

import android.os.Handler;
import android.os.Message;

import com.shine.usbcameralib.serialdog.ARMSerial;
import com.shine.usbcameralib.serialdog.SerialManager;
import com.shine.usbcameralib.serialdog.serialutil.LogPlus;
import com.shine.usbcameralib.serialdog.serialutil.Toolkit;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * ARM芯片的读取串口线程
 *
 * @author 郭鹏
 * @date 15/9/7
 * @since v1.0.0
 */
public class ARMThread extends SerialThread {

    private SerialPort mSerialPort = null;

    private boolean myFlg = true;
    private int check = 0;
    private int dog = 0;


    public ARMThread(Handler handler, String path, int baudrate, ARMSerial armSerial) {
        super(handler, path, baudrate, armSerial);
    }

    @Override
    public void closeSerialPort() {
        myFlg = false;
        if (mOutputStream != null) {
            try {
                LogPlus.i("关狗");
                armSerial.closeDog(mOutputStream);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            isOpen = false;
        }
        mSerialPort = null;
    }

    @Override
    public Object showSerialPort() {
        return mSerialPort;
    }

    @Override
    public void doClose() {
        if (mOutputStream != null) {
            try {
                armSerial.doClose(mOutputStream);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeDog() {
        if (mOutputStream != null) {
            try {
                LogPlus.i("关狗");
                armSerial.closeDog(mOutputStream);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {

            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        }
        return mSerialPort;
    }

    @Override
    public void run() {
        super.run();
        LogPlus.i("开启了线程");
        try {
            mSerialPort = getSerialPort();
            if (mOutputStream != null) {
                LogPlus.i("开狗");
                armSerial.openDog(mOutputStream);
                mOutputStream.flush();
                Message message = handler.obtainMessage();
                message.what = SerialManager.RECEIVE_STARTCHECK;
                message.sendToTarget();
            }
            while (myFlg) {
                int size;
                if (mInputStream == null) {
                    return;
                }
                byte[] buffer = new byte[mInputStream.available()];
                size = mInputStream.read(buffer);
                if (size > 0) {
                    onDataReceived(Toolkit.bytes2Hex(buffer), mOutputStream);
                }

                if (check == 200) {
                    armSerial.checkHeart(mOutputStream);
                    mOutputStream.flush();
                }

                if (dog == 400) {
                    armSerial.checkDog(mOutputStream);
                    mOutputStream.flush();
                }

                check++;
                dog++;

                if (check >= 200 * 30) {
                    check = 0;
                }

                if (dog >= 200 * 20) {
                    dog = 0;
                }

                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            myFlg = false;
            e.printStackTrace();
            sendReSetMessage();
        }
    }
}
