package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Date;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class EclipseDayPointingActivity extends AppCompatActivity
implements MyTimer.MyTimerListener{

    private CalibrateDirectionFragment calibrateDirectionFragment;

    private MyTimer mTimer;
    private EclipseTimeProvider eclipseTimeProvider;
    private Long midTotalityTime;

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

        Button captureModeButton = (Button) findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCaptureMode();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    public void goToCaptureMode() {
        Intent intent = new Intent(this,EclipseDayCaptureActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTick() {
        midTotalityTime = eclipseTimeProvider.getPhaseTimeMills(EclipseTimingMap.Event.MIDDLE);
        if (midTotalityTime != null && calibrateDirectionFragment.shouldUseCurrentTime) {
            calibrateDirectionFragment.setTargetTimeMills(midTotalityTime);
            calibrateDirectionFragment.setShouldUseCurrentTime(false);
        }
    }
}
