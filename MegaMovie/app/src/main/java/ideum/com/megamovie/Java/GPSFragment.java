package ideum.com.megamovie.Java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class GPSFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        LocationProvider {

    public  long gpsInterval = 1000 * 30;
    public  long fastestGpsInterval = 1000 * 10;
    public int locationRequestPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private static final boolean SHOULD_USE_DUMMY_LOCATION = true;
    private static final double DUMMY_LATITUDE = -45;
    private static final double DUMMY_LONGITUDE = -71;


    private int REQUEST_LOCATION_PERMISSIONS = 0;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<LocationListener> locationListeners = new ArrayList<>();

    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public Location getLocation() {
        if (mGoogleApiClient == null) {
            return null;
        }
        Location lastLocation = null;

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (lastLocation == null) {
            return null;
        }
        if (SHOULD_USE_DUMMY_LOCATION) {
            lastLocation.setLatitude(DUMMY_LATITUDE);
            lastLocation.setLongitude(DUMMY_LONGITUDE);
        }

        return lastLocation;
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
        mLocationRequest.setInterval(gpsInterval);
        mLocationRequest.setFastestInterval(fastestGpsInterval);
        mLocationRequest.setPriority(locationRequestPriority);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (SHOULD_USE_DUMMY_LOCATION) {
            location.setLatitude(DUMMY_LATITUDE);
            location.setLongitude(DUMMY_LONGITUDE);
        }

        for(LocationListener listener: locationListeners) {
            listener.onLocationChanged(location);
        }
    }
}
