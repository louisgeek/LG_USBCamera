package com.shine.usbcamera;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private TextureView mPreviewTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,}, 1);

        mPreviewTextureView = findViewById(R.id.tv_preview);
        mPreviewTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
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

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            // 当Surface可用的时候打开摄像头
            openUSUCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

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
                        mUVCCamera.setPreviewTexture(mPreviewTextureView.getSurfaceTexture());
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
