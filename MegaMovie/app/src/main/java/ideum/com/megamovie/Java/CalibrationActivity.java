package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

import ideum.com.megamovie.R;

public class CalibrationActivity extends AppCompatActivity {

    private GPSFragment mGPSFragment;
    private CountdownFragment mCountdownFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        mCountdownFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mCountdownFragment != null) {
            mCountdownFragment.isPrecise = true;
            mCountdownFragment.setLocationProvider(mGPSFragment);
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }
    }

    public void loadCaptureActivity(View view) {
        startActivity(new Intent(this, CaptureActivity.class));
    }

    public void loadMapActivity(View view) {
        startActivity(new Intent(this, MapActivity.class));
    }
}

