package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.CameraControl.CaptureSequence;
import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class MoonTestCalibrateDirectionActivity extends AppCompatActivity {

    private CalibrateDirectionFragment calibrateDirectionFragment;
    private CameraPreviewAndCaptureFragment mCameraFragment;

    private Button nextButton;
    private Button previousButton;

    private Planet target;

    private static final long EXPOSURE_TIME = 5000000L;
    private static int SENSOR_SENSITIVITY = 60;
    private static float FOCUS_DISTANCE = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_calibrate_direction);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.shouldUseCurrentTime = true;
        setTargetFromSettings();

        calibrateDirectionFragment.resetModelCalibration();
        calibrateDirectionFragment.showView(false);

        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_preview_fragment);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonPressed();
            }
        });

//        previousButton = (Button) findViewById(R.id.back_button);
//        previousButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onPreviousButtonPressed();
//            }
//        });

        Long testTime = getTestTimeFromSettings();

        Date testTimeDate = new Date(testTime);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd HH:mm a");


        mCameraFragment.setDirectoryName("Megamovie practice " + dateFormatter.format(testTimeDate));

    }

    private void enterPointingMode() {
        Intent intent = new Intent(this, MoonTestPointingActivity.class);
        startActivity(intent);
    }

    public void calibrateToTarget(View view) {
        calibrateDirectionFragment.calibrateModelToTarget();
    }

    public void resetCalibration(View view) {
        calibrateDirectionFragment.resetModelCalibration();
    }

    public void useCurrentTime(View view) {
        calibrateDirectionFragment.setShouldUseCurrentTime(true);
    }

    private void setTargetFromSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String targetName = preferences.getString(getString(R.string.sun_moon_test_target), Planet.Moon.name());
        Planet planet = Planet.Moon;
        if (targetName.equals(Planet.Sun.name())) {
            planet = Planet.Sun;
        }
        setTarget(planet);
    }

    private void setTarget(Planet planet) {
        target = planet;
        calibrateDirectionFragment.setTarget(target);
    }

    private void captureImage() {
        boolean shouldSaveRaw = false;
        boolean shouldSaveJPEG = true;
        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(EXPOSURE_TIME,
                SENSOR_SENSITIVITY,
                FOCUS_DISTANCE,
                shouldSaveRaw,
                shouldSaveJPEG);
        mCameraFragment.takePhotoWithSettings(settings);
    }

    private void onNextButtonPressed() {
        calibrateToTarget(null);
        captureImage();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                enterPointingMode();
            }
        }, 1000);
    }

    private void onPreviousButtonPressed() {
        finish();
    }

    public void dim(View view) {
        if (mCameraFragment != null) {
            mCameraFragment.decrementDuration(10);

        }
    }

    public void brighten(View view) {
        if (mCameraFragment != null) {
            mCameraFragment.incrementDuration(10);
        }
    }

    private Long getTestTimeFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int year = prefs.getInt(getString(R.string.test_time_year), -1);
        int month = prefs.getInt(getString(R.string.test_time_month), -1);
        int dayOfMonth = prefs.getInt(getString(R.string.test_time_day_of_month), -1);
        int hours = prefs.getInt(this.getString(R.string.test_time_hour), -1);
        int minutes = prefs.getInt(getString(R.string.test_time_minute), -1);
        if (hours == -1
                || minutes == -1
                || year == -1
                || month == -1
                || dayOfMonth == -1) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }


}
