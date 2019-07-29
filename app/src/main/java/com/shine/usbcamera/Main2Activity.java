package com.shine.usbcamera;

import android.Manifest;
import android.hardware.usb.UsbDevice;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";

    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private GLSurfaceView mPreviewSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,}, 1);

        mPreviewSurfaceView = findViewById(R.id.tv_glsurfaceview);
        mPreviewSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                gl10.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            }

            @Override
            public void onSurfaceChanged(GL10 gl10, int w, int h) {
                gl10.glViewport(0, 0, w, h);
            }

            @Override
            public void onDrawFrame(GL10 gl10) {
                gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            }
        });
        mPreviewSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "surfaceCreated");
                // 当Surface可用的时候打开摄像头
                openUSUCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
        mUSBMonitor = new USBMonitor(this, onDeviceConnectListener);
        mUSBMonitor.register();

    }

    // 请求打开USB摄像头，SystemUI已修改 不会弹出对话框等待用户确认； 直接授权
    private void openUSUCamera() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
        List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter);
        if (deviceList.size() > 0) {
            mUSBMonitor.requestPermission(deviceList.get(0));
        } else {
            Log.e(TAG, "onCreate: no camera device");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mUVCCamera != null) {
            mUVCCamera.startPreview();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUVCCamera != null) {
            mUVCCamera.stopPreview();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCamera();
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    private void releaseCamera() {
        if (mUVCCamera != null) {
            mUVCCamera.destroy();
            mUVCCamera = null;
        }
    }

    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
            }
            mUVCCamera = new UVCCamera();
            new Thread() {
                @Override
                public void run() {
                    mUVCCamera.open(ctrlBlock);
                    try {
                        mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                                UVCCamera.FRAME_FORMAT_YUYV);
                        mUVCCamera.setPreviewDisplay(mPreviewSurfaceView.getHolder());
                        mUVCCamera.startPreview();
                    } catch (final IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (mUVCCamera != null) {
                mUVCCamera.close();
            }
        }

        @Override
        public void onDetach(final UsbDevice device) {
            Log.d(TAG, "onDetach() called with: device = [" + device + "]");
        }

        @Override
        public void onCancel() {
        }
    };

}
