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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
        LocationProvider,
        MyTimer.MyTimerListener{

    private boolean cameraShouldMoveToCurrentLocation = true;
    private CountdownFragment mCountdownFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private static final String TAG = "Main Activity";
    private static final long THRESHOLD_TIME_SECONDS = 20;
    private ContentResolver mContentResolver;

    /**
     * Request code for location permissions
     */
    /**
     * Request code for camera permissions
     */
    private static final int REQUEST_PERMISSIONS = 2;

    /**
     * Permissions required to take a picture.
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng mCurrentLocation;
    private GoogleMap mGoogleMap;
    private GPSFragment mGPSFragment;
    private ContactTimesFragment mContactTimesFragment;
    private MyTimer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Allow app to control screen brightness to save power
        mContentResolver = getContentResolver();

        if (!checkSystemWritePermissions()) {
            requestPermissionWriteSettings();
        }
        if (checkSystemWritePermissions()) {
            Settings.System.putInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        // request all permissions for later use
        if (!hasAllPermissionsGranted()) {
            requestAllPermissions();
        }

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.addLocationListener(this);
        mGPSFragment.locationRequestPriority = LocationRequest.PRIORITY_LOW_POWER;

        mContactTimesFragment = (ContactTimesFragment) getFragmentManager().findFragmentById(R.id.contact_times_fragment);
        mContactTimesFragment.setLocationProvider(this);
        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext(), this);
            mContactTimesFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCountdownFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);

        if (mCountdownFragment != null) {
            mCountdownFragment.includesDays = true;
            mCountdownFragment.setLocationProvider(this);
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public Location getLocation() {
        return mGPSFragment.getLocation();
    }

    @Override
    public void onTick() {
        if(isWithinTimeThreshold()) {
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
        mTimer.addListener(this);
        mTimer.startTicking();

        cameraShouldMoveToCurrentLocation = true;
        if (mGoogleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    private boolean isWithinTimeThreshold() {
        if (mGPSFragment.getLocation() == null) {
            return false;
        }
        Location location = getLocation();
        Long firstContactTime = mEclipseTimeCalculator.getEclipseTime(location, EclipseTimeCalculator.Event.CONTACT2);
        Long currentTime = mGPSFragment.getLocation().getTime();
        Long delta_time_seconds = (firstContactTime - currentTime)/1000;
        return delta_time_seconds < THRESHOLD_TIME_SECONDS;
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

    private void updateMarkers() {
        if (mGoogleMap == null) {
            return;
        }
        mGoogleMap.clear();
        if (mCurrentLocation != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(mCurrentLocation));
            if (cameraShouldMoveToCurrentLocation) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
            }
        }
        // We want camera to move to current position when it first sets marker, but not after that
        // but not to move automatically after that
        cameraShouldMoveToCurrentLocation = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mContactTimesFragment.updateTextViews();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mCurrentLocation = new LatLng(latitude, longitude);
        updateMarkers();
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

    private void loadCaptureActivity() {
        startActivity(new Intent(this, CaptureActivity.class));

    }

}
