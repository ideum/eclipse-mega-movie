package ideum.com.megamovie.Java.CameraControl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import ideum.com.megamovie.R;

public class CameraHardwareCheckActivity extends AppCompatActivity {

    /**
     * Request code for camera permissions
     */
    private static final int REQUEST_CAMERA_PERMISSIONS = 0;

    /**
     * Permissions required to take a picture.
     */
    private static final String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA};
    private CameraManager mCameraManager;
    private int hardwareLevel;
    private TextView hardwareFullTextView;
    private boolean manualSensorSupported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_hardware_check);
        hardwareFullTextView = (TextView) findViewById(R.id.hardware_full_text_view);

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkHardwareLevel();
    }

    private void checkHardwareLevel() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraID : mCameraManager.getCameraIdList()) {
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraID);
                if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                 hardwareLevel = cc.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                int[] supportedCapabilities = cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                for (int i = 0; i< supportedCapabilities.length; i++) {
                    if (supportedCapabilities[i] == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) {
                        manualSensorSupported = true;
                    }
                }

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        updateUI();
    }


    private void updateUI() {
       String s = "Camera Capability Unknown";
//        switch (hardwareLevel) {
//            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
//                s = "Unfortunately, your phone's camera does not support manual sensor control";
//                break;
//            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
//                s = "Unfortunately, your phone's camera does not support manual sensor control";
//                break;
//            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
//                s = "Your phone's camera is supported!";
//                break;
//            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
//                s = "Your phone's camera is supported!";
//
//        }
        if (manualSensorSupported) {
            s = "Manual Control of sensors is supported";
        } else {
            s = "Manual Control of sensors is not supported";
        }
        hardwareFullTextView.setText(s);
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
