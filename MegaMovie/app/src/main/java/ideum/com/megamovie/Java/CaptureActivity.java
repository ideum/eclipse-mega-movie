
package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.HashMap;

import ideum.com.megamovie.R;

public class CaptureActivity extends AppCompatActivity
implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        MyTimer.MyTimerListener{

    private final static String TAG = "CaptureActivity";
    private static int TIMER_LENGTH = 300;
    private static int TIMER_INTERVAL = 300;

    private long mSensorExposureTime;
    private int mSensorSensitivity;
    private float mLensFocusDistance;

    private int REQUEST_LOCATION_PERMISSIONS = 0;


    private CameraFragment mCameraFragment;
    private TimerFragment mTimerFragment;
    private MyTimer mTimer;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

    private Calendar mCalendar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        Log.e(TAG,"activity created");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }

        mTimerFragment = (TimerFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        EclipseTimeCalculator eclipseTimeCalculator = new EclipseTimeCalculator();
        if (mTimerFragment != null) {
            mTimerFragment.isPrecise = true;
            mTimerFragment.setTargetDateMills(eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT1,new LatLng(0,0)));
        }
        mCameraFragment = new CameraFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mCameraFragment).commit();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();

        Resources res = getResources();
        ConfigParser parser = new ConfigParser(res.getXml(R.xml.config));
        EclipseCaptureSequenceBuilder builder = new EclipseCaptureSequenceBuilder(new LatLng(0,0),parser);
        CaptureSequence sequence = builder.buildSequence();
        CaptureSequenceTimer session = new CaptureSequenceTimer(mCameraFragment,sequence);
        session.startTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new MyTimer(this);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    @Override
    public void onTick() {
        mTimerFragment.updateDisplay();
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this,CalibrationActivity.class));
    }
    public void loadResultsActivity(View view) {
        startActivity(new Intent(this,ResultsActivity.class));
    }

    public void startCaptureSequence(View view) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        mCameraFragment.setLocation(mLocation);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
    }
}
