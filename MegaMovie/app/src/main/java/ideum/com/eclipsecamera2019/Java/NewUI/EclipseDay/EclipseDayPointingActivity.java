package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.eclipsecamera2019.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.eclipsecamera2019.Java.provider.ephemeris.Planet;
import ideum.com.eclipsecamera2019.R;

public class EclipseDayPointingActivity extends AppCompatActivity{

    private CalibrateDirectionFragment calibrateDirectionFragment;

    private EclipseTimeProvider eclipseTimeProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_pointing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        eclipseTimeProvider = new EclipseTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, eclipseTimeProvider).commit();

        calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.calibrateModelFromSettings();
        calibrateDirectionFragment.setTarget(Planet.Sun);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Long midTime = preferences.getLong(getString(R.string.mid_time_key),0);
        calibrateDirectionFragment.setTargetTimeMills(midTime);
        calibrateDirectionFragment.setShouldUseCurrentTime(false);

        Button captureModeButton = (Button) findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCaptureMode();
            }
        });
    }



    public void goToCaptureMode() {
        Intent intent = new Intent(this,EclipseDayCaptureActivity.class);
        startActivity(intent);
    }


}
