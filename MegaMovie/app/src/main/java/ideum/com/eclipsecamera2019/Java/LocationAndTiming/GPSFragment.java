package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssClock;
import android.location.Location;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ideum.com.eclipsecamera2019.Java.Application.Config;


public class GPSFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        LocationProvider,
        LocationSource,
        android.location.LocationListener {

    public long gpsInterval = 1000 * 30;
    public long fastestGpsInterval = 1000 * 20;
    public int locationRequestPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private static final int MINIMUM_DISTANCE_BEFORE_UPDATE_METRES = 2000;
    private static final int LOCATION_UPDATE_TIME_MILLISECONDS = 0;

    private int REQUEST_LOCATION_PERMISSIONS = 0;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<OnLocationChangedListener> locationListeners = new ArrayList<>();
    private long timeOffset;
    private boolean timeCalibrated  = false;
    private LocationManager mLocationManager;
    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
//        if(mLocationManager != null) {
//            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if(mLocation != null) {
//                for (OnLocationChangedListener listener : locationListeners) {
//                    listener.onLocationChanged(getAdjustedLocation());
//                }
//            }
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }

         mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLocation != null) {
            for (OnLocationChangedListener listener : locationListeners) {
                listener.onLocationChanged(getAdjustedLocation());
            }
        }
        //Criteria locationCriteria = new Criteria();
        //locationCriteria.setAccuracy(Criteria.ACCURACY_HIGH);
        //String provider = locationManager.getBestProvider(locationCriteria,true);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0f,this);

    }


    public Location getLocation() {
        return mLocation;
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

    private Location mLocation;
    private Location getAdjustedLocation() {
        Location aLocation = new Location(mLocation);
        if (Config.SHOULD_USE_DUMMY_LOCATION) {
            aLocation.setLatitude(Config.DUMMY_LATITUDE);
            aLocation.setLongitude(Config.DUMMY_LONGITUDE);
        }

        return aLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
      mLocation = location;

        for (OnLocationChangedListener listener : locationListeners) {
            listener.onLocationChanged(getAdjustedLocation());
        }
        long systemTime = Calendar.getInstance().getTimeInMillis();
        long gpsTime = location.getTime();
        // Only want to set the time correction once, to make sure time doesn't jump too much
        // not monotonically
        if(!timeCalibrated) {
            timeOffset = gpsTime - systemTime;
            timeCalibrated = true;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationListeners.add(onLocationChangedListener);
    }

    @Override
    public void deactivate() {

    }
//
//    @Override
//    public Long getCurrentTimeMillis() {
//        return Calendar.getInstance().getTimeInMillis() + timeOffset;
//    }

}
