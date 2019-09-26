package com.shine.usbcameralib.system.light;

/**
 * 灯光控制接口
 *
 * @author GP
 * @date 2016/8/18.
 */
public interface LightInterface {
    void doLightOn();
    void doLightAllOn();
    void setLight(int light);
    void reLight();
    int getLight();
}
