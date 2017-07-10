package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class MoonTestCalibrateDirectionActivity extends AppCompatActivity {

    private CalibrateDirectionFragment calibrateDirectionFragment;
    private CameraPreviewAndCaptureFragment mCameraFragment;
    private TextView targetTextView;
    private TextView methodTextView;
    private TextView testTimeTextView;

    private Button nextButton;
    private int state = 0;

    private Planet target;
    private int calibrationMethod;
    private String testTimeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_calibrate_direction);

        targetTextView = (TextView) findViewById(R.id.target_text_view);
        methodTextView = (TextView) findViewById(R.id.method_text_view);
        testTimeTextView = (TextView) findViewById(R.id.test_time_text_view);

        calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.shouldUseCurrentTime = true;
        setTargetFromSettings();
        setCalibrationMethodFromSettings();
        setTestTimeMillsFromSettings();
//        calibrateDirectionFragment.showView(false);

        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_preview_fragment);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonPressed();
            }
        });

    }

    public void calibrateToTarget(View view) {
        calibrateDirectionFragment.calibrateModelToMoon();
    }

    public void useCurrentTime(View view) {
        calibrateDirectionFragment.setShouldUseCurrentTime(true);
    }

    public void useTargetTime(View view) {
        Long targetTime = setTestTimeMillsFromSettings();
        calibrateDirectionFragment.setTargetTimeMills(targetTime);
        calibrateDirectionFragment.setShouldUseCurrentTime(false);
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

    private void setCalibrationMethodFromSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int method = preferences.getInt(getString(R.string.calibration_method),0);
        setCalibrationMethod(method);
    }

    private void setCalibrationMethod(int method) {
        calibrationMethod = method;
        calibrateDirectionFragment.useMethod(method);
        updateUI();
    }

    private Long setTestTimeMillsFromSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int hour = preferences.getInt(getString(R.string.test_time_hour), -1);
        int minute = preferences.getInt(getString(R.string.test_time_minute), -1);
        if (hour == -1 || minute == -1) {
            return null;
        }
        setTestTimeString(hour,minute);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void setTestTimeString(int hour,int minute) {
        hour = hour % 12;
        testTimeString = String.valueOf(hour) + ":" + String.valueOf(minute);
        updateUI();

    }



    private void setTarget(Planet planet) {
        target = planet;
        calibrateDirectionFragment.setTarget(target);
        updateUI();
    }

    private void updateUI() {
        targetTextView.setText("Target: " + target.name());
        methodTextView.setText("Method: " + String.valueOf(calibrationMethod));
        testTimeTextView.setText("Test time: " + testTimeString);
    }

    private void onNextButtonPressed() {
        if (state == 0) {
            nextButton.setText("Finish");
            calibrateDirectionFragment.showView(true);
            useCurrentTime(null);
            state = 1;

            useTargetTime(null);

//            android.app.FragmentManager fragmentManager = getFragmentManager();
//            android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.remove(mCameraFragment);
//            transaction.commit();

        } else if (state == 1) {
            finish();
        }
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


}
