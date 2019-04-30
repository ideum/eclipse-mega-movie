package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import ideum.com.eclipsecamera2019.Java.Application.MyApplication;
import ideum.com.eclipsecamera2019.R;

import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c1;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c3;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c4;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.cm;

/**
 * Returns the time of the eclipse phases based on the phone's most recent gps location,
 * and caches the result as persistent settings data
 */
public class EclipseTimeProvider extends Fragment
        implements LocationSource.OnLocationChangedListener, LocationProvider {

    public interface Listener {
        void onEclipseTimesUpdated(EnumMap<EclipseTimes.Phase,Long> contactTimes);
    }
    private List<Listener> listeners = new ArrayList<>();

    public void addListener(Listener l) {
        listeners.add(l);
    }
    private GPSFragment mGPSFragment;
    protected EclipseTimes mEclipseTimes;
    private Location mLocation;

    EnumMap<EclipseTimes.Phase,Long> contactTimes = new EnumMap<>(EclipseTimes.Phase.class);
    boolean inPathOfTotality;
    public EclipseTimeProvider() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();

        mGPSFragment = new GPSFragment();
        getActivity().getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.activate(this);
        MyApplication ma = (MyApplication) getActivity().getApplication();
        mEclipseTimes = ma.eclipseTimes;
      initializeEclipseTimes();
        if(inPathOfTotality) {
            notifyListeners();
        }
    }

    protected void initializeEclipseTimes() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        contactTimes.put(c1, preferences.getLong(getString(R.string.c1_time_key), 0));
        contactTimes.put(c2, preferences.getLong(getString(R.string.c2_time_key), 0));
        contactTimes.put(cm, preferences.getLong(getString(R.string.mid_time_key), 0));
        contactTimes.put(c3, preferences.getLong(getString(R.string.c3_time_key), 0));
        contactTimes.put(c4, preferences.getLong(getString(R.string.c4_time_key), 0));
        inPathOfTotality = preferences.getBoolean(getString(R.string.in_path_key),true);

    }

    protected LatLng getLatLng() {
        if (mLocation == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            float lat = preferences.getFloat(getString(R.string.current_lat_key),0);
            float lng = preferences.getFloat(getString(R.string.current_lng_key),0);
            return new LatLng(lat,lng);
        }
        return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        inPathOfTotality = !(EclipsePath.distanceToPathOfTotality(mLocation) > 0);
        storeInPath();
        refreshEclipseTimes();
        storeEclipseTimes();
        notifyListeners();
    }

    protected void refreshEclipseTimes() {

        Long c1Time = mEclipseTimes.getEclipseTime(c1, getLatLng());
        Long c2Time = mEclipseTimes.getEclipseTime(c2, getLatLng());
        Long cmTime = mEclipseTimes.getEclipseTime(cm, getLatLng());
        Long c3Time = mEclipseTimes.getEclipseTime(c3, getLatLng());
        Long c4Time = mEclipseTimes.getEclipseTime(c4, getLatLng());

        contactTimes.put(c1,c1Time);
        contactTimes.put(c2,c2Time);
        contactTimes.put(cm,cmTime);
        contactTimes.put(c3,c3Time);
        contactTimes.put(c4,c4Time);
    }

    private void storeInPath() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.in_path_key), inPathOfTotality);
        editor.putFloat(getString(R.string.current_lat_key),(float)mLocation.getLatitude());
        editor.putFloat(getString(R.string.current_lng_key),(float)mLocation.getLongitude());
        editor.commit();
    }

    private void storeEclipseTimes() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        if (contactTimes.get(c1) != null) {
            editor.putLong(getString(R.string.c1_time_key), contactTimes.get(c1));
        }
        if (contactTimes.get(c2) != null) {
            editor.putLong(getString(R.string.c2_time_key), contactTimes.get(c2));
        }
        if (contactTimes.get(cm) != null) {
            editor.putLong(getString(R.string.mid_time_key), contactTimes.get(cm));
        }
        if (contactTimes.get(c3) != null) {
            editor.putLong(getString(R.string.c3_time_key), contactTimes.get(c3));
        }
        if (contactTimes.get(c4) != null) {
            editor.putLong(getString(R.string.c4_time_key), contactTimes.get(c4));
        }
        editor.commit();
    }

    protected EnumMap<EclipseTimes.Phase,Long> getContactTimes() {
        return contactTimes;
    }

    protected void notifyListeners() {
        if(inPath()){
            for(Listener l : listeners) {
                l.onEclipseTimesUpdated(getContactTimes());
            }
        }
    }

    public boolean inPath() {
        return inPathOfTotality;
    }

    public Long getPhaseTimeMills(EclipseTimes.Phase phase) {

        Long eventTime = contactTimes.get(phase);
        if(eventTime == null) {
            return null;
        }


        return eventTime;
    }

    @Override
    public Location getLocation() {
        return mLocation;
    }



}
