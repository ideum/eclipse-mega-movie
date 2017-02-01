package ideum.com.megamovie.Java;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity
implements MyTimer.MyTimerListener{
    private TimerFragment mTimerFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private Location mCurrentLocation;
    private MyTimer mTimer;

    @Override
    public void onTick() {
        mTimerFragment.updateDisplay();
    }

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
            mTimerFragment.isPrecise = true;
            mTimerFragment.setTargetDateMills(mEclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Contact.CONTACT1,new LatLng(0,0)));
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
}

