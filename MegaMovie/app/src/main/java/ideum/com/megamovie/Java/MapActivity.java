package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import ideum.com.megamovie.R;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        MyTimer.MyTimerListener {

    private GoogleMap mGoogleMap;
    private GPSFragment mGPSFragment;
    private ContactTimesFragment mContactTimesFragment;
    private EclipseCountdownFragment mCountdownFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private MyTimer mTimer;
    private Location mLocation;
    private TextView latLngTextView;
    private static final String TAG = "Main Activity";

    /**
     * Switch to capture mode with 20 seconds until first contact
     */
    private static final long THRESHOLD_TIME_SECONDS = 40;
    /**
     * Resolver is used to interact with system settings to be able
     * to control screen brightness
     */
    private ContentResolver mContentResolver;

    /**
     * Request code for permissions
     */
    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        /**
         * Keep activity in portrait mode
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /**
         * Allow app to control screen brightness to save power
         */
        mContentResolver = getContentResolver();

        if (!checkSystemWritePermissions()) {
            requestPermissionWriteSettings();
        }
        if (checkSystemWritePermissions()) {
            Settings.System.putInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        /**
         * Request all permissions for later use
         */
        if (!hasAllPermissionsGranted()) {
            requestAllPermissions();
        }

        latLngTextView = (TextView) findViewById(R.id.lat_lng_text_view);

        /**
         * Set up gps
         */
        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.addLocationListener(this);
//        mGPSFragment.locationRequestPriority = LocationRequest.PRIORITY_LOW_POWER;

        /**
         * Set up eclipse time calculator and the fragment displaying contact times
         */
        mContactTimesFragment = (ContactTimesFragment) getFragmentManager().findFragmentById(R.id.contact_times_fragment);
        mContactTimesFragment.setLocationProvider(mGPSFragment);
        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext(), mGPSFragment);
            mContactTimesFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Set up countdown fragment
         */
        mCountdownFragment = (EclipseCountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);

        if (mCountdownFragment != null) {
            mCountdownFragment.includesDays = true;
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }

        /**
         * Get the SupportMapFragment and request notification
         * when the map is ready to be used.
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onTick() {
        if (isWithinTimeThreshold()) {
            mTimer.cancel();
            loadCaptureActivity();
        }
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);

    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new MyTimer();
        mTimer.addListener(mCountdownFragment);

        /**
         * Listen to timer to determine when to switch to capture mode
         */
        mTimer.addListener(this);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onPause();
    }

    private boolean isWithinTimeThreshold() {
        if (mEclipseTimeCalculator == null) {
            return false;
        }
        Long millsToContact2 = mEclipseTimeCalculator.getTimeToEvent(EclipseTimeCalculator.Event.CONTACT2);

        if (millsToContact2 == null) {
            return false;
        }

        Long secondsToContact2 = millsToContact2/1000;

        return secondsToContact2 < THRESHOLD_TIME_SECONDS;
    }

    private boolean checkSystemWritePermissions() {
        boolean permission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
        } else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_DENIED;
        }
        return permission;
    }

    private void requestPermissionWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }


    private void updateLatLngTextView(double lat, double lng) {
        if (latLngTextView != null) {
            latLngTextView.setText("Latitiude: " + String.valueOf(lat) + "\nlongitude: " + String.valueOf(lng));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        updateLatLngTextView(lat,lng);
        LatLng currentLocation = new LatLng(lat, lng);
        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions().position(currentLocation));

        /**
         * When first get gps coordinates move camera to current location
         */
        if (mLocation == null) {
            mLocation = location;

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        }

        mContactTimesFragment.updateTextViews();
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

    public void loadCameraTestActivity(View view) {
        startActivity(new Intent(this, CameraTestActivity.class));
    }

    private void loadCaptureActivity() {
        startActivity(new Intent(this, CaptureActivity.class));
    }

}
