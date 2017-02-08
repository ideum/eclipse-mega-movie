package ideum.com.megamovie.Java;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;

import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity
implements MyTimer.MyTimerListener{

    public final static String TAG = "CALIBRATION_ACTIVITY";
    private GPSFragment mGPSFragment;
    private CountdownFragment mCountdownFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    // switch to capture mode with 30 seconds until first contact
    private static final long THRESHOLD_TIME_SECONDS = 15;

    private MyTimer mTimer;
    private CameraPreviewAndCaptureFragment mPreviewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        mCountdownFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mCountdownFragment != null) {
            mCountdownFragment.isPrecise = true;
            mCountdownFragment.setLocationProvider(mGPSFragment);
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }

         mTimer = new MyTimer(this);
         mTimer.startTicking();

        mPreviewFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.preview_fragment);
        Resources res = getResources();
        ConfigParser parser = new ConfigParser(res.getXml(R.xml.config));
        CaptureSequence.CaptureSettings settings = parser.getSettings();
        mPreviewFragment.setCameraSettings(settings);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private boolean isWithinTimeThreshold() {
        if (mGPSFragment.getLocation() == null) {
            return false;
        }
        Long firstContactTime = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT1,new LatLng(0,0));
        Long currentTime = mGPSFragment.getLocation().getTime();
        Long delta_time_seconds = (firstContactTime - currentTime)/1000;
        Log.e(TAG,String.valueOf(delta_time_seconds));
        Log.e(TAG,String.valueOf(delta_time_seconds < THRESHOLD_TIME_SECONDS));
        return delta_time_seconds < THRESHOLD_TIME_SECONDS;
    }

    @Override
    public void onTick() {
//        if(isWithinTimeThreshold()) {
//            mTimer.cancel();
//            loadCaptureActivity();
//        }
    }

    private void loadCaptureActivity() {
        startActivity(new Intent(this, CaptureActivity.class));

    }

    private void loadMapActivity() {
        startActivity(new Intent(this, MapActivity.class));

    }

    public void loadCaptureActivityButtonPressed(View view) {
        loadCaptureActivity();
    }

    public void loadMapActivityButtonPressed(View view) {
        loadMapActivity();
    }
}

