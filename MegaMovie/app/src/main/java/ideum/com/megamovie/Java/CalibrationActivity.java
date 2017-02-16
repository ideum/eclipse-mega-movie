package ideum.com.megamovie.Java;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity
implements MyTimer.MyTimerListener {

    public final static String TAG = "CALIBRATION_ACTIVITY";
    private GPSFragment mGPSFragment;
    private CountdownFragment mCountdownFragment;
    private CameraPreviewAndCaptureFragment mPreviewFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private MyTimer mTimer;

    /**
     *  Switch to capture mode with 20 seconds until first contact
     */
    private static final long THRESHOLD_TIME_SECONDS = 20;
    private static final boolean COUNTDOWN_TIMER_SHOWS_DAYS = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        // Keep phone from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Fix screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        mCountdownFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);

        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext(),mGPSFragment);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mCountdownFragment != null) {
            mCountdownFragment.includesDays = COUNTDOWN_TIMER_SHOWS_DAYS;
            mCountdownFragment.setLocationProvider(mGPSFragment);
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }

        mPreviewFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.preview_fragment);

        try {
            // Set up preview using initial camera settings from config file
            Resources resources = getResources();
            ConfigParser parser = new ConfigParser(resources);
            CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(parser.getIntervalProperties().get(0));
            mPreviewFragment.setCameraSettings(settings);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTimer = new MyTimer();


        mTimer.addListener(this);

        mTimer.addListener(mCountdownFragment);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        super.onPause();
    }

    public Location getLocation() {
        return mGPSFragment.getLocation();
    }



    @Override
    public void onTick() {
        if(isWithinTimeThreshold()) {
            mTimer.cancel();
            loadCaptureActivity();
        }
    }

    // Check whether it is time to move to capture activity
    private boolean isWithinTimeThreshold() {
        if (mGPSFragment.getLocation() == null) {
            return false;
        }
        Location location = getLocation();
        Long firstContactTime = mEclipseTimeCalculator.getEclipseTime(location, EclipseTimeCalculator.Event.CONTACT2);
        if (firstContactTime == null) {
            return false;
        }
        Long currentTime = mGPSFragment.getLocation().getTime();
        Long delta_time_seconds = (firstContactTime - currentTime)/1000;
        return delta_time_seconds < THRESHOLD_TIME_SECONDS;
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

