package ideum.com.megamovie.Java.LocationAndTiming;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.R;



public class EclipseTimeProvider extends Fragment
implements LocationSource.OnLocationChangedListener,
LocationProvider{

    private static final Boolean USE_DUMMY_C2 = false;

    private Boolean inPathOfTotality = true;

    private Long dummyC2Time;
    private Location mLocation;

    private GPSFragment mGPSFragment;
    private EclipseTimeLocationManager mEclipseTimeManager;

    public EclipseTimeProvider() {
        // Required empty public constructor
    }

    public boolean inPathOfTotality() {
        return inPathOfTotality;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dummyC2Time = Calendar.getInstance().getTimeInMillis() + 10 * 1000;
    }


    @Override
    public void onResume() {
        super.onResume();

        mGPSFragment = new GPSFragment();
        getActivity().getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.activate(this);

        MyApplication ma = (MyApplication) getActivity().getApplication();
        EclipseTimeCalculator eclipseTimeCalculator = ma.getEclipseTimeCalculator();


        mEclipseTimeManager = new EclipseTimeLocationManager(eclipseTimeCalculator,getActivity().getApplicationContext());
        mEclipseTimeManager.setAsLocationListener(mGPSFragment);
        mEclipseTimeManager.shouldUseCurrentLocation = true;
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        if (EclipsePath.distanceToPathOfTotality(location) > 0) {
            inPathOfTotality = false;
        } else {
            inPathOfTotality = true;
        }



        mEclipseTimeManager.setCurrentLatLng(latLng);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.in_path_key),inPathOfTotality);

        Long c2Time = getPhaseTimeMills(EclipseTimingMap.Event.CONTACT2);
        Long middleTime = getPhaseTimeMills(EclipseTimingMap.Event.MIDDLE);
        Long c3Time = getPhaseTimeMills(EclipseTimingMap.Event.CONTACT3);
        if (c2Time != null) {
            editor.putLong(getString(R.string.c2_time_key),c2Time);
        }
        if (middleTime != null) {
            editor.putLong(getString(R.string.mid_time_key),middleTime);
        }
        if (c3Time != null) {
            editor.putLong(getString(R.string.c3_time_key),c3Time);
        }

        editor.commit();




    }

    public Long getPhaseTimeMills(EclipseTimingMap.Event event) {
        if (mEclipseTimeManager == null) {
            return null;
        }
        Long eventTime = mEclipseTimeManager.getEclipseTime(event);
        if (eventTime == null) {
            return null;
        }

        if (USE_DUMMY_C2) {
            Long correction = dummyC2Time - mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT2);
            eventTime += correction;
        }


        return eventTime;
    }

    public Long getStartOfTotalityMills() {
        if (mEclipseTimeManager == null) {
            return null;
        }
        return mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT2);
    }

    public String getContactTimeString(EclipseTimingMap.Event event) {
        return mEclipseTimeManager.getContactTimeString(event);
    }

    @Override
    public Location getLocation() {
        return mLocation;
    }
}
