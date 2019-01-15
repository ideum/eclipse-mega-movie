package ideum.com.megamovie.Java.LocationAndTiming;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.EnumMap;

import ideum.com.megamovie.Java.Application.Config;
import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.R;

import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c1;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c3;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c4;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.cm;

/**
 * Returns the time of the eclipse phases based on the phone's most recent gps location,
 * and caches the result as persistent settings data
 */
public class EclipseTimeProvider extends Fragment
        implements LocationSource.OnLocationChangedListener, LocationProvider {

    private GPSFragment mGPSFragment;
    private EclipseTimes mEclipseTimes;
    private Location mLocation;
    private Long dummyC2Time;

    EnumMap<EclipseTimes.Phase,Long> contactTimes = new EnumMap<>(EclipseTimes.Phase.class);
    boolean inPathOfTotality;
    public EclipseTimeProvider() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dummyC2Time = Calendar.getInstance().getTimeInMillis() + 15 * 1000;
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        contactTimes.put(c1, preferences.getLong(getString(R.string.c1_time_key), 0));
        contactTimes.put(c2, preferences.getLong(getString(R.string.c2_time_key), 0));
        contactTimes.put(cm, preferences.getLong(getString(R.string.mid_time_key), 0));
        contactTimes.put(c3, preferences.getLong(getString(R.string.c3_time_key), 0));
        contactTimes.put(c4, preferences.getLong(getString(R.string.c4_time_key), 0));
        inPathOfTotality = preferences.getBoolean(getString(R.string.in_path_key),true);
    }

    private LatLng getLatLng() {
        if (mLocation == null) {
            return null;
        }
        return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        inPathOfTotality = !(EclipsePath.distanceToPathOfTotality(location) > 0);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.in_path_key), inPathOfTotality);

        Long c1Time = mEclipseTimes.getEclipseTime(c1, getLatLng());
        Long c2Time = mEclipseTimes.getEclipseTime(c2, getLatLng());
        Long cmTime = mEclipseTimes.getEclipseTime(cm, getLatLng());
        Long c3Time = mEclipseTimes.getEclipseTime(c3, getLatLng());
        Long c4Time = mEclipseTimes.getEclipseTime(c4, getLatLng());

        if (c1Time != null) {
            editor.putLong(getString(R.string.c1_time_key), c1Time);
        }
        if (c2Time != null) {
            editor.putLong(getString(R.string.c2_time_key), c2Time);
        }
        if (cmTime != null) {
            editor.putLong(getString(R.string.mid_time_key), cmTime);
        }
        if (c3Time != null) {
            editor.putLong(getString(R.string.c3_time_key), c3Time);
        }
        if (c4Time != null) {
            editor.putLong(getString(R.string.c4_time_key), c4Time);
        }

        editor.commit();

        contactTimes.put(c1,c1Time);
        contactTimes.put(c2,c2Time);
        contactTimes.put(cm,cmTime);
        contactTimes.put(c3,c3Time);
        contactTimes.put(c4,c4Time);
    }

    public boolean inPath() {
        return inPathOfTotality;
    }

    public Long getPhaseTimeMills(EclipseTimes.Phase phase) {

        Long eventTime = contactTimes.get(phase);
        if(eventTime == null) {
            return null;
        }

        if (Config.USE_DUMMY_TIME_C2) {
            Long correction = dummyC2Time - mEclipseTimes.getEclipseTime(c2, getLatLng());
            eventTime += correction;
        }
        if (Config.USE_DUMMY_TIME_ALL_CONTACTS) {
            switch (phase) {
                case c2:
                    return dummyC2Time;
                case cm:
                    return dummyC2Time + 3000;
                case c3:
                    return dummyC2Time + 6000;
                case c4:
                    return dummyC2Time + 9000;
            }
        }

        return eventTime;
    }

    @Override
    public Location getLocation() {
        return mLocation;
    }
}
