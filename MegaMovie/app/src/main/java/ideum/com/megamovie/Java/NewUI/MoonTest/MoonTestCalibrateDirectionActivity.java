package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private TextView instructionsTextView;

    private Button nextButton;
    private Button previousButton;

    private int state = 0;
    private static final int MAX_STATE = 1;

    private Planet target;
    private int calibrationMethod;
    private String testTimeString;
    private String instructionsString;
    private String nextButtonString;
    private String previousButtonString;

    private List<String> instructions = new ArrayList<>();
    private List<String> nextButtonStrings = new ArrayList<>();
    private List<String> previousButtonStrings = new ArrayList<>();

    private void setState(int s) {
        state = s;
        if (s > MAX_STATE || s < 0) {
            finish();
        }
        if (s == 0) {
            useCurrentTime(null);
//            calibrateDirectionFragment.showView(false);

        }
        if (s == 1) {
            useTargetTime(null);
//            calibrateDirectionFragment.showView(true);
        }

        if (s < instructions.size() && s >= 0) {
            instructionsString = instructions.get(s);
        }
        if (s < nextButtonStrings.size() && s >= 0) {
            nextButtonString = nextButtonStrings.get(s);
        }
        if (s < previousButtonStrings.size() && s >= 0) {
            previousButtonString = previousButtonStrings.get(s);
        }
        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_calibrate_direction);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        instructions.add(0,getString(R.string.moon_test_calibrate_compass_instructions));
        instructions.add(1,getString(R.string.moon_test_point_phone_instructions));
        nextButtonStrings.add(0,"NEXT");
        nextButtonStrings.add(1,"FINISH");
        previousButtonStrings.add(0,"CANCEL");
        previousButtonStrings.add(1,"PREVIOUS");

        targetTextView = (TextView) findViewById(R.id.target_text_view);
        methodTextView = (TextView) findViewById(R.id.method_text_view);
        testTimeTextView = (TextView) findViewById(R.id.test_time_text_view);
        instructionsTextView = (TextView) findViewById(R.id.calibrate_direction_instructions_text_view);

        calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.shouldUseCurrentTime = true;
        setTargetFromSettings();
        setCalibrationMethodFromSettings();
        setTestTimeMillsFromSettings();

        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_preview_fragment);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonPressed();
            }
        });

        previousButton = (Button) findViewById(R.id.back_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPreviousButtonPressed();
            }
        });

        setState(0);
    }

    public void calibrateToTarget(View view) {
        calibrateDirectionFragment.calibrateModelToMoon();
    }

    public void resetCalibration(View view) {
        calibrateDirectionFragment.resetModelCalibration();
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int year = prefs.getInt(getString(R.string.test_time_year),-1);
        int month = prefs.getInt(getString(R.string.test_time_month),-1);
        int dayOfMonth = prefs.getInt(getString(R.string.test_time_day_of_month),-1);
        int hours = prefs.getInt(this.getString(R.string.test_time_hour),-1);
        int minutes = prefs.getInt(getString(R.string.test_time_minute),-1);
        if (hours == -1
                || minutes == -1
                || year == -1
                || month == -1
                || dayOfMonth == -1){
            return null;
        }
        setTestTimeString(hours,minutes);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY,hours);
        c.set(Calendar.MINUTE,minutes);
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
        if (targetTextView != null) {
            targetTextView.setText("Target: " + target.name());
        }
        if (methodTextView != null) {
            methodTextView.setText("Method: " + String.valueOf(calibrationMethod));
        }
        if (testTimeTextView != null) {
            testTimeTextView.setText("Test time: \n" + testTimeString);
        }
        if (instructionsTextView != null) {
            instructionsTextView.setText(instructionsString);
        }
        if (nextButton != null) {
            nextButton.setText(nextButtonString);
        }
        if (previousButton != null) {
            previousButton.setText(previousButtonString);
        }
    }

    private void onNextButtonPressed() {
        if (state == 0) {
            calibrateToTarget(null);
        }
        setState(state + 1);
    }

    private void onPreviousButtonPressed() {
        if (state == 1) {
            resetCalibration(null);
        }
        setState(state - 1);

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
