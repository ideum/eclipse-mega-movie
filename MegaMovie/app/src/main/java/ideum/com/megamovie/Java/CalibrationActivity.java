package ideum.com.megamovie.Java;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity
implements MyTimer.MyTimerListener,
LocationProvider{
    private CountdownFragment mTimerFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private Location mCurrentLocation;
    private MyTimer mTimer;

    @Override
    public Location getLocation() {
        return mCurrentLocation;
    }

    @Override
    public void onTick() {
        mTimerFragment.updateDisplay();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        mTimerFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        mEclipseTimeCalculator = new EclipseTimeCalculator();
        if (mTimerFragment != null) {
            mTimerFragment.isPrecise = true;
            mTimerFragment.setLocationProvider(this);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mTimer = new MyTimer(this);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    public void loadCaptureActivity(View view) {
        startActivity(new Intent(this,CaptureActivity.class));
    }
    public void loadMapActivity(View view) {
        startActivity(new Intent(this,MapActivity.class));
    }
}

