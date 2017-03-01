package ideum.com.megamovie.Java;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity
        implements MyTimer.MyTimerListener {

    public final static String TAG = "CALIBRATION_ACTIVITY";
    private GPSFragment mGPSFragment;
    private EclipseCountdownFragment mCountdownFragment;
    private CameraPreviewAndCaptureFragment mPreviewFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private MyTimer mTimer;
    private CheckBox narrowFieldCheckbox;
    private final static String NARROW_FIELD_PREF = "USES_LENS";
    private static final int NARROW_FIELD_CONFIG_ID = R.xml.narrow_field_config;
    private boolean mIsNarrowField;


    /**
     * Switch to capture mode with 20 seconds until first contact
     */
    private static final long THRESHOLD_TIME_SECONDS = 40;
    private static final boolean COUNTDOWN_TIMER_SHOWS_DAYS = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        // Keep phone from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Fix screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences preferences = getPreferences(0);
        boolean isNarrowField = preferences.getBoolean(NARROW_FIELD_PREF, false);
        narrowFieldCheckbox = (CheckBox) findViewById(R.id.narrow_field_checkbox);
        narrowFieldCheckbox.setChecked(isNarrowField);
        mIsNarrowField = isNarrowField;

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        mCountdownFragment = (EclipseCountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);

        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext(), mGPSFragment);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mCountdownFragment != null) {
            mCountdownFragment.includesDays = COUNTDOWN_TIMER_SHOWS_DAYS;
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }

        mPreviewFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.preview_fragment);

        try {
            setCameraSettings();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void setCameraSettings() throws IOException, XmlPullParserException {
        // Set up preview using initial camera settings from narrow_field_config file
        Resources resources = getResources();
        ConfigParser parser = new ConfigParser(resources, NARROW_FIELD_CONFIG_ID);
        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(parser.getIntervalProperties().get(0));
        settings.sensitivity = 200;
        settings.exposureTime = 1000000;
        mPreviewFragment.setCameraSettings(settings);
    }

    private void setIsNarrowField(boolean isNarrowField) {
        SharedPreferences preferences = getPreferences(0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NARROW_FIELD_PREF, isNarrowField);
        mIsNarrowField = isNarrowField;
        editor.commit();
    }

    public void narrowFieldCheckboxClicked(View view) {
        CheckBox checkBox = (CheckBox) view;
        boolean isChecked = checkBox.isChecked();
        setIsNarrowField(isChecked);
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
        if (isWithinTimeThreshold()) {
            mTimer.cancel();
            loadCaptureActivity();
        }
    }

    // Check whether it is time to move to capture activity
    private boolean isWithinTimeThreshold() {
        if (mEclipseTimeCalculator == null) {
            return false;
        }
        Long millsToContact2 = mEclipseTimeCalculator.getTimeToEvent(EclipseTimeCalculator.Event.CONTACT2);

        if (millsToContact2 == null) {
            return false;
        }

        Long secondsToContact2 = millsToContact2 / 1000;

        return secondsToContact2 < THRESHOLD_TIME_SECONDS;
    }

    private void loadCaptureActivity() {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra("IS_NARROW_FIELD", mIsNarrowField);
        startActivity(intent);

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

