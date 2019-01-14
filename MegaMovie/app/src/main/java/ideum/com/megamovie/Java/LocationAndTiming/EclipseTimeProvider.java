package ideum.com.megamovie.Java.LocationAndTiming;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

import ideum.com.megamovie.Java.Application.Config;
import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.R;

import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c3;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.cm;

/**
 * Returns the time of the eclipse phases based on the phone's most recent gps location,
 * and caches the result as persistent settings data
 */
public class EclipseTimeProvider extends Fragment
        implements LocationSource.OnLocationChangedListener, LocationProvider {

    private GPSFragment mGPSFragment;
    private EclipseTimeLocationManager mEclipseTimeManager;
    private Location mLocation;
    private Boolean inPathOfTotality = true;
    private Long dummyC2Time;

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
        //EclipseTimeCalculator eclipseTimeCalculator = ma.getEclipseTimeCalculator();
        mEclipseTimeManager = new EclipseTimeLocationManager(ma.eclipseTimes, getActivity().getApplicationContext());
        mEclipseTimeManager.setAsLocationListener(mGPSFragment);
        mEclipseTimeManager.shouldUseCurrentLocation = true;
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (EclipsePath.distanceToPathOfTotality(location) > 0) {
            inPathOfTotality = false;
        } else {
            inPathOfTotality = true;
        }

        mEclipseTimeManager.setCurrentLatLng(latLng);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.in_path_key), inPathOfTotality);

        Long c2Time = getPhaseTimeMills(c2);
        Long middleTime = getPhaseTimeMills(cm);
        Long c3Time = getPhaseTimeMills(c3);
        if (c2Time != null) {
            editor.putLong(getString(R.string.c2_time_key), c2Time);
            Log.i("EclipseTimeProvider","storing c2 time " + String.valueOf(c2Time));
        }
        if (middleTime != null) {
            editor.putLong(getString(R.string.mid_time_key), middleTime);
        }
        if (c3Time != null) {
            editor.putLong(getString(R.string.c3_time_key), c3Time);
        }

        editor.commit();

    }

    public Long getPhaseTimeMills(EclipseTimes.Phase phase) {
        if (mEclipseTimeManager == null) {
            return null;
        }
        Long eventTime = mEclipseTimeManager.getEclipseTime(phase);
        if (eventTime == null) {
            return null;
        }

        if (Config.USE_DUMMY_TIME_C2) {
            Long correction = dummyC2Time - mEclipseTimeManager.getEclipseTime(c2);
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
