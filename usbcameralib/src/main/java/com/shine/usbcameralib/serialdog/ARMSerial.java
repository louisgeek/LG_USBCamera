package com.shine.usbcameralib.serialdog;

import java.io.IOException;
import java.io.OutputStream;

/**
 * ARM串口数据
 *
 * @author GP
 * @date 2016/8/31.
 */
public interface ARMSerial {
    void openLight(OutputStream outputStream) throws IOException;
    void closeLight(OutputStream outputStream) throws IOException;
    void openDog(OutputStream outputStream) throws IOException;
    void closeDog(OutputStream outputStream) throws IOException;
    void checkDog(OutputStream outputStream) throws IOException;
    void checkHeart(OutputStream outputStream) throws IOException;
    void onDataReceive(String string, OutputStream outputStream);
    void doClose(OutputStream outputStream) throws IOException;
}
