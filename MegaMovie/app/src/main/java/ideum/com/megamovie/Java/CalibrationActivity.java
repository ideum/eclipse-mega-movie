package ideum.com.megamovie.Java;

import android.content.Intent;
import android.icu.lang.UCharacterEnums;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity {
    private TimerFragment mTimerFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private Location mCurrentLocation;


    public void loadCaptureActivity(View view) {
        startActivity(new Intent(this,CaptureActivity.class));
    }
    public void loadMapActivity(View view) {
        startActivity(new Intent(this,MapActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        mTimerFragment = (TimerFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        mEclipseTimeCalculator = new EclipseTimeCalculator();
        if (mTimerFragment != null) {
            mTimerFragment.setTargetDateMills(mEclipseTimeCalculator.calculateEclipseTimeInMills(0, 0));
        }
    }
}
