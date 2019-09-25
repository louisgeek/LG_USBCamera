/**
 * com.bed.nurse.shine.thread
 * 
 * @version 1.0.0
 *
 * Copyright (C) 2015-2015 神州视瀚.
 */
package com.shine.usbcameralib.serialdog.thread;

import android.os.Handler;
import android.os.Message;

import com.shine.usbcameralib.serialdog.ARMSerial;
import com.shine.usbcameralib.serialdog.SerialManager;
import com.shine.usbcameralib.serialdog.serialutil.LogPlus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 读取串口线程
 * 
 * @author 郭鹏
 * @date 15/9/1
 * @since v1.0.0
 */
public abstract class SerialThread extends Thread {

  protected Handler handler;
  protected String path;
  protected int baudrate;
  protected OutputStream mOutputStream;
  protected InputStream mInputStream;
  private String temp = "";
  public boolean isOpen = false;
  protected ARMSerial armSerial;

  public SerialThread(Handler handler, String path, int baudrate, ARMSerial armSerial) {
    this.handler = handler;
    this.path = path;
    this.baudrate = baudrate;
    this.armSerial = armSerial;
  }

  protected void onDataReceived(String str, OutputStream outputStream) {
      armSerial.onDataReceive(str, outputStream);
  }

  public void onDestroy() {
    LogPlus.i("SerialThread onDestroy");
    closeSerialPort();
  }

  public void sendReSetMessage() {
    LogPlus.i("sendReSetMessage");
    closeSerialPort();
    Message message = handler.obtainMessage();
    message.what = SerialManager.SERIALDISCONNECT;
    message.sendToTarget();
  }

  public abstract void closeSerialPort();

  public abstract Object showSerialPort();

  public void openLight() {
    if (mOutputStream != null) {
      try {
        armSerial.openLight(mOutputStream);
        mOutputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void closeLight() {
    if (mOutputStream != null) {
      try {
        armSerial.closeLight(mOutputStream);
        mOutputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public abstract void doClose();

  public abstract void closeDog();
}
