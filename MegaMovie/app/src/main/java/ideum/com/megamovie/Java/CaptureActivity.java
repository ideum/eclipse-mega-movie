
package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import ideum.com.megamovie.R;

public class CaptureActivity extends AppCompatActivity
        implements CameraFragment.CaptureListener,
        CaptureSequenceSession.CameraController,
        LocationListener {

    private final static String TAG = "CaptureActivity";
    private GPSFragment mGPSFragment;
    private CameraFragment mCameraFragment;
    private MyTimer mTimer;
    private CaptureSequenceSession session;
    private TextView captureTextView;
    private Integer totalCaptures;
    private static final String[] SETTINGS_PERMISSIONS = {Manifest.permission.WRITE_SETTINGS};

    /**
     * Resolver used for interacting with system settings to adjust screen brightness
     */
    private ContentResolver mContentResolver;

    /**
     * Whether to dim screen to save battery power
     */
    private static final boolean SHOULD_DIM_SCREEN = false;

    /**
     * Screen brightness saved before dimming
     */
    private int initialBrightness;

    /**
     * Brightness after dimming to save power
     */
    private static final int SCREEN_BRIGHTNESS_LOW = 5;

    private Location mLocation;

    @Override
    public void onCapture() {
        updateCaptureTextView();
    }

    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        mCameraFragment.takePhotoWithSettings(settings);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        /**
         *  Keep phone from going to sleep
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContentResolver = getContentResolver();

        /**
         * Initial view showing number of completed captures
         */
        captureTextView = (TextView) findViewById(R.id.capture_text);
        /**
         * Add Gps
         */
        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.addLocationListener(this);

        /**
         * Add Camera Fragment
         */
        mCameraFragment = new CameraFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mCameraFragment).commit();
        mCameraFragment.setLocationProvider(mGPSFragment);
        mCameraFragment.addCaptureListener(this);
    }

    private boolean checkSystemWritePermissions() {
        boolean permission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
        } else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_DENIED;
        }
        return permission;
    }

    private void setUpCaptureSequenceSession() {
        try {
            Resources resources = getResources();
            ConfigParser parser = new ConfigParser(resources);
            EclipseTimeCalculator calculator = new EclipseTimeCalculator(getApplicationContext(), mGPSFragment);
            EclipseCaptureSequenceBuilder builder = new EclipseCaptureSequenceBuilder(mGPSFragment, parser, calculator);
            CaptureSequence sequence = builder.buildSequence();

            totalCaptures = sequence.getRequestQueue().size();
            updateCaptureTextView();

            /**
             * Create and start the capture sequence session
             */
            session = new CaptureSequenceSession(sequence, mGPSFragment, this);
            mTimer = new MyTimer();
            mTimer.addListener(session);
            mTimer.startTicking();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Save the current brightness then dim the screen
         */
        try {
            initialBrightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (SHOULD_DIM_SCREEN) {
            setScreenBrightness(SCREEN_BRIGHTNESS_LOW);
        }
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (SHOULD_DIM_SCREEN) {
            setScreenBrightness(initialBrightness);
        }
        super.onPause();
    }


    @Override
    public void onLocationChanged(Location location) {

        /**
         * The first time the gps coordinates are available, create the capture sequence session
         */
        if (mLocation == null) {
            mLocation = location;
            setUpCaptureSequenceSession();
        }
    }

    private void setScreenBrightness(int brightness) {
        if (!checkSystemWritePermissions()) {
            return;
        }
        Settings.System.putInt(mContentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness);
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
}
