package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
        ActivityCompat.OnRequestPermissionsResultCallback{
//        ,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        LocationListener,
//        LocationProvider {

    private boolean cameraShouldMoveToCurrentLocation = true;
    private CountdownFragment mCountdownFragment;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private static final String TAG = "Main Activity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // request all permissions for later use
        if (!hasAllPermissionsGranted()) {
            requestAllPermissions();
        }

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        mGPSFragment.addLocationListener(this);

        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCountdownFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);

        if (mCountdownFragment != null) {
            mCountdownFragment.isPrecise = true;
            mCountdownFragment.setLocationProvider(mGPSFragment);
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        cameraShouldMoveToCurrentLocation = true;
        if (mGoogleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mCurrentLocation = new LatLng(latitude, longitude);
        updateMarkers();
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

}
