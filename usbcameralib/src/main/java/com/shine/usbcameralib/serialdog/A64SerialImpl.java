package com.shine.usbcameralib.serialdog;

import android.content.Intent;
import android.view.KeyEvent;

import com.shine.usbcameralib.SeriableBedLibProvider;
import com.shine.usbcameralib.serialdog.bean.KeyUp;
import com.shine.usbcameralib.serialdog.serialutil.LogPlus;
import com.shine.usbcameralib.serialdog.serialutil.PrefUtils;
import com.shine.usbcameralib.serialdog.serialutil.RxBus;
import com.shine.usbcameralib.serialdog.serialutil.Toolkit;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A64串口
 * Created by shine-gp on 18-3-22.
 */

public class A64SerialImpl implements ARMSerial {

    private String temp = "";

    private SerialManager serialManager;

    private String dogVersion = "";

    public A64SerialImpl(SerialManager serialManager) {
        this.serialManager = serialManager;
        String dog_version = PrefUtils.getString(SeriableBedLibProvider.provideAppContext(), "dog_version", "0");
        dogVersion = dog_version;
    }

    @Override
    public void openLight(OutputStream outputStream) throws IOException {

    }

    @Override
    public void closeLight(OutputStream outputStream) throws IOException {

    }

    @Override
    public void openDog(OutputStream outputStream) throws IOException {
        LogPlus.i("开狗");
        outputStream.write(Toolkit.hex2Bytes("7E10020000026EAA"));
    }

    @Override
    public void closeDog(OutputStream outputStream) throws IOException {
        LogPlus.i("closeDog.");
        outputStream.write(Toolkit.hex2Bytes("7E10020000016DAA"));
    }

    @Override
    public void checkDog(OutputStream outputStream) throws IOException {
        LogPlus.i("checkDog.");
        outputStream.write(Toolkit.hex2Bytes("7E10040000006AAA"));
    }

    @Override
    public void checkHeart(OutputStream outputStream) throws IOException {
        LogPlus.i("checkHeart.");
        outputStream.write(Toolkit.hex2Bytes("7E10050000016AAA"));
    }

    @Override
    public void onDataReceive(String string, OutputStream outputStream) {
        LogPlus.i("string:" + string);
        try {
            temp += string;
            if (temp.length() == 16) {
                temp = temp.replace("\r\n", "");
                if (temp.startsWith("7E") && temp.endsWith("AA")) {
                    receive(temp, outputStream);
                }
                temp = "";
            } else if (temp.length() > 16) {
                String result = temp.substring(0, 16);
                result = result.replace("\r\n", "");
                if (result.startsWith("7E") && result.endsWith("AA")) {
                    receive(result, outputStream);
                }
                result = temp.substring(16, temp.length());
                temp = "";
                onDataReceive(result, outputStream);
            }
        } catch (Exception e) {
            LogPlus.i("接收出错");
            e.printStackTrace();
        }
    }

    @Override
    public void doClose(OutputStream outputStream) throws IOException {
        LogPlus.i("doClose.");
        outputStream.write(Toolkit.hex2Bytes("7E100800001472AA"));
    }

    private void receive(String temp, OutputStream outputStream) {

        LogPlus.i("result:" + temp);

        String from = temp.substring(2, 4);
        String action = temp.substring(4, 6);
        int result = Integer.parseInt(temp.substring(12, 14), 16);
        String version = temp.substring(10, 12);
        String press = temp.substring(8, 10);
        int checkResult = Integer.parseInt("7E", 16) ^
                Integer.parseInt(from, 16) ^
                Integer.parseInt(action, 16) ^
                Integer.parseInt(temp.substring(6, 8), 16) ^
                Integer.parseInt(temp.substring(8, 10), 16) ^
                Integer.parseInt(temp.substring(10, 12), 16);
        if (result != checkResult) return;
        if (from.equals("01")) {
            switch (action) {
                case "06":
                    String statue = temp.substring(8, 10);
                    if (!version.equals(dogVersion)) {
                        dogVersion = version;
                        PrefUtils.getString(SeriableBedLibProvider.provideAppContext(), "dog_version", version);
                    }
                    if (statue.equals("02")) {
                        serialManager.serialHandler.
                                removeCallbacks(serialManager.restartSerial);
                        serialManager.serialHandler.
                                postDelayed(serialManager.restartSerial, 45 * 1000);
                    }
                    break;
                case "01":
                    KeyUp keyUp = new KeyUp();
                    keyUp.keyCode = 82;
                    keyUp.keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyUp.keyCode);
                    RxBus.INSTANCE.post(keyUp);
                    Intent intent = new Intent("com.android.server.PhoneWindowManager.action.EXTKEYEVENT");
                    intent.putExtra("scanCode",139);
                    SeriableBedLibProvider.provideAppContext()
                            .sendBroadcast(intent);
                    LogPlus.i("A64SerialImpl", "手屏按键" + keyUp.keyCode);
                    checkResult = Integer.parseInt("7E", 16) ^
                            Integer.parseInt("10", 16) ^
                            Integer.parseInt("07", 16) ^
                            Integer.parseInt("00", 16) ^
                            Integer.parseInt(press, 16) ^
                            Integer.parseInt("00", 16);
                    try {
                        outputStream.write(Toolkit.hex2Bytes("7E100700" + press + "00"
                                + Integer.toHexString(checkResult).toUpperCase() + "AA"));
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}