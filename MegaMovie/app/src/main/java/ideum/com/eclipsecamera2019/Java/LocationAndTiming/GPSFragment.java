package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssClock;
import android.location.Location;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import ideum.com.eclipsecamera2019.Java.NewUI.WarningDialogFragment;
import ideum.com.eclipsecamera2019.Java.OrientationController.Clock;
import ideum.com.eclipsecamera2019.R;


public class GPSFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        LocationProvider,
        LocationSource,
        Clock,
        android.location.LocationListener {


    final private int REQUEST_LOCATION_PERMISSIONS = 2;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<OnLocationChangedListener> locationListeners = new ArrayList<>();
    private long timeOffset = 0;
    private boolean timeCalibrated  = false;
    private LocationManager mLocationManager;
    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION_PERMISSIONS);
        } else {
            setup();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        timeOffset = preferences.getLong(getString(R.string.time_offset_key),0);

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

    private void setup(){
        try {
            mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLocation != null) {
                for (OnLocationChangedListener listener : locationListeners) {
                    listener.onLocationChanged(getAdjustedLocation());
                }
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, this);
        } catch(SecurityException e){
            Log.e("PERMISSION", "NO PERMISSION GIVEN");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
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
        storeTimeOffset(gpsTime - systemTime);
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

    private void storeTimeOffset(Long offset) {
        Context c = getContext();
        if (c == null || offset == null) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(getString(R.string.time_offset_key), offset);
        editor.commit();
    }


    @Override
    public long getTimeInMillisSinceEpoch() {
        return Calendar.getInstance().getTimeInMillis() + timeOffset;
    }
}
