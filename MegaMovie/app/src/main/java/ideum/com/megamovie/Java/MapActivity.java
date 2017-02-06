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
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        LocationProvider {

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
    private int REQUEST_LOCATION_PERMISSIONS = 0;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng mCurrentLocation;
    private LatLng mPlannedLocation;
    private GoogleMap mGoogleMap;


    public void loadUserInfoActivity(View view) {
        startActivity(new Intent(this, UserInfoActivity.class));
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

    @Override
    public Location getLocation() {
        Location lastLocation = null;
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (lastLocation == null) {
            return null;
        }
        return lastLocation;    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Request permission to access location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }

        SharedPreferences preferences = getPreferences(getApplicationContext().MODE_PRIVATE);
        double lat = (double) preferences.getFloat("PLANNED_LATITUDE", 0);
        double lon = (double) preferences.getFloat("PLANNED_LONGITUDE", 0);
        mPlannedLocation = new LatLng(lat, lon);

        mCountdownFragment = (CountdownFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        try {
            mEclipseTimeCalculator = new EclipseTimeCalculator(getApplicationContext());
            mEclipseTimeCalculator.getEclipseTime(new LatLng(45,70));
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (mCountdownFragment != null) {
            mCountdownFragment.isPrecise = true;
            mCountdownFragment.setLocationProvider(this);
            mCountdownFragment.setEclipseTimeCalculator(mEclipseTimeCalculator);
        }
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        createLocationRequest();
        // Create an instance of the GoogleApiClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraShouldMoveToCurrentLocation = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
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
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Washington D.C.,
        // and move the map's camera to the same location
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mPlannedLocation = point;
                updateMarkers();
                SharedPreferences preferences = getPreferences(getApplicationContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putFloat("PLANNED_LATITUDE", (float) point.latitude);
                editor.putFloat("PLANNED_LONGITUDE", (float) point.longitude);
                editor.commit();
            }
        });
    }

    private void updateMarkers() {
        mGoogleMap.clear();
        if (mPlannedLocation != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(mPlannedLocation));
        }
        if (mCurrentLocation != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(mCurrentLocation));
            if (cameraShouldMoveToCurrentLocation) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
            }
        }
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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mCurrentLocation = new LatLng(latitude, longitude);

        updateMarkers();
        // We want camera to move to current position when it first finds gps coordinates
        // but not to move automatically after that
        cameraShouldMoveToCurrentLocation = false;

    }

}
