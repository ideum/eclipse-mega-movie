
package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import ideum.com.megamovie.R;

public class CaptureActivity extends AppCompatActivity
        implements CameraFragment.CaptureListener {

    private final static String TAG = "CaptureActivity";
    private int REQUEST_LOCATION_PERMISSIONS = 0;
    private GPSFragment mGPSFragment;
    private CameraFragment mCameraFragment;
    private TextView captureTextView;
    private Integer totalCaptures;
    private CaptureSequenceSession session;
    private static final int SETTINGS_PERMISSIONS_REQUEST_CODE = 5;
    private static final String[] SETTINGS_PERMISSIONS = {Manifest.permission.WRITE_SETTINGS};
    private int initialBrightness;
    private ContentResolver mContentResolver;

    @Override
    public void onCapture() {
        updateCaptureTextView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        // Allow app to control screen brightness to save power
        mContentResolver = getContentResolver();


        // Initial view showing number of completed captures
        captureTextView = (TextView) findViewById(R.id.capture_text);

        /* Add Gps */
        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        /* Add Camera Fragment */
        mCameraFragment = new CameraFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mCameraFragment).commit();
        mCameraFragment.setLocationProvider(mGPSFragment);
        mCameraFragment.addCaptureListener(this);

//        setUpCaptureSequenceSession();

    }



    private boolean checkSystemWritePermissions() {
        boolean permission = true;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             permission = Settings.System.canWrite(this);
         } else {
             permission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_DENIED;
         }
        return permission;
    }

    private void setUpCaptureSequenceSession() {
        Resources res = getResources();
        ConfigParser parser = new ConfigParser(res.getXml(R.xml.config));
        try {
            EclipseTimeCalculator calculator = new EclipseTimeCalculator(getApplicationContext());
            EclipseCaptureSequenceBuilder builder = new EclipseCaptureSequenceBuilder(new LatLng(0, 0), parser, calculator);
            CaptureSequence sequence = builder.buildSequence();
            session = new CaptureSequenceSession(mCameraFragment, sequence, mGPSFragment);
            session.startSession();
            totalCaptures = sequence.getTimedRequests().size();
            updateCaptureTextView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session == null) {
            setUpCaptureSequenceSession();
        }
        setScreenBrightness(0);
    }


    @Override
    protected void onPause() {
        if (session != null) {
            session.cancelSession();
            session = null;
        }
        setScreenBrightness(initialBrightness);
        super.onPause();
    }


    private void setScreenBrightness(int brightness) {
        if (!checkSystemWritePermissions()) {
            return;
        }
        try {
            initialBrightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.putInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateCaptureTextView() {
        if (captureTextView == null) {
            return;
        }
        captureTextView.setText("Images Captured: " + String.valueOf(mCameraFragment.mRequestCounter) + "/" + String.valueOf(totalCaptures));
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

    public void loadResultsActivity(View view) {
        startActivity(new Intent(this, ResultsActivity.class));
    }
}
