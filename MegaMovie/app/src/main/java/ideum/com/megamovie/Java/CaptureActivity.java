
package ideum.com.megamovie.Java;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ideum.com.megamovie.R;

public class CaptureActivity extends AppCompatActivity
implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static String TAG = "CaptureActivity";
    private static int TIMER_LENGTH = 10000;
    private static int TIMER_INTERVAL = 500;
    private static long SENSOR_EXPOSURE_TIME = 500000;
    private static int SENSOR_SENSITIVITY = 720;
    private static float LENS_FOCUS_DISTANCE = 3.0f;
    private CameraFragment mCameraFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

    private Calendar mCalendar;

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this,CalibrationActivity.class));
    }
    public void loadResultsActivity(View view) {
        startActivity(new Intent(this,ResultsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
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
        mCalendar = Calendar.getInstance();

        CaptureSequence.CaptureSettings s = new CaptureSequence.CaptureSettings(5000000, 100, 0);
        long startTime = getTime();

        EclipseCaptureSequenceBuilder builder = new EclipseCaptureSequenceBuilder(new LatLng(0,0));

        CaptureSequence sequence =  builder.buildSequence();
        CaptureSequenceTimer cst = new CaptureSequenceTimer(mCameraFragment, sequence);
//        cst.startTimer();
    }

    private long getTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private HashMap<Long,CaptureSequence.CaptureSettings> mTimedRequests;
    public void startCaptureSequence(View view) {
        startTimer();
    }

    public void startTimer() {
        new CountDownTimer(TIMER_LENGTH, TIMER_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                mCameraFragment.takePhoto(SENSOR_EXPOSURE_TIME,SENSOR_SENSITIVITY,LENS_FOCUS_DISTANCE);
                Log.e(TAG,"Tick");
            }

            public void onFinish() {
                    Toast.makeText(getApplicationContext(), "done!", Toast.LENGTH_SHORT).show();
            }
        }.start();
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
