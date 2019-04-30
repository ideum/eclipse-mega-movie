package ideum.com.eclipsecamera2019.Java.CameraControl;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import ideum.com.eclipsecamera2019.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraHardwareCheckFragment extends Fragment {


    /**
     * Request code for camera permissions
     */
    private static final int REQUEST_CAMERA_PERMISSIONS = 0;

    /**
     * Permissions required to take a picture.
     */
    private static final String[] CAMERA_PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private CameraManager mCameraManager;
    private int hardwareLevel;
    private TextView hardwareFullTextView;
    private boolean manualSensorSupported;


    public CameraHardwareCheckFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
        }

        checkHardwareLevel();
    }

    public boolean isCameraSupported() {
        return manualSensorSupported;
    }

    private void checkHardwareLevel() {
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
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
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(getString(R.string.camera_supported_key),true);
                        editor.commit();

                    }
                }

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(getActivity(), CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
