package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class MoonTestPointingActivity extends AppCompatActivity {

    private CalibrateDirectionFragment calibrateDirectionFragment;

    private Planet target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_pointing);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.calibrateModelFromSettings();
        setTargetFromSettings();
//        setCalibrationMethodFromSettings();
        setTestTimeFromSettings();

        Button captureModeButton = (Button) findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCaptureMode();
            }
        });

    }

    private void enterCaptureMode() {
        Intent intent = new Intent(this, MoonTestCaptureActivity.class);
        startActivity(intent);

    }

    private void setTestTimeFromSettings() {
        Long targetTime = getTestTimeFromSettings();
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


    private void setTarget(Planet planet) {
        target = planet;
        calibrateDirectionFragment.setTarget(target);
        updateUI();
    }

    private void updateUI() {

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
        return c.getTimeInMillis();
    }
}
