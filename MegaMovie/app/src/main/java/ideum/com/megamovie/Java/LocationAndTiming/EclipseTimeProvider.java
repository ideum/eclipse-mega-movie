package ideum.com.megamovie.Java.LocationAndTiming;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.R;



public class EclipseTimeProvider extends Fragment
implements LocationSource.OnLocationChangedListener{


    private GPSFragment mGPSFragment;
    private EclipseTimeLocationManager mEclipseTimeManager;

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
        EclipseTimeCalculator eclipseTimeCalculator = ma.getEclipseTimeCalculator();


        mEclipseTimeManager = new EclipseTimeLocationManager(eclipseTimeCalculator,getActivity().getApplicationContext());
        mEclipseTimeManager.setAsLocationListener(mGPSFragment);
        mEclipseTimeManager.shouldUseCurrentLocation = true;
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mEclipseTimeManager.setCurrentLatLng(latLng);
    }

    public Long getPhaseTimeMills(EclipseTimingMap.Event event) {
        if (mEclipseTimeManager == null) {
            return null;
        }
        return mEclipseTimeManager.getEclipseTime(event);
    }

    public Long getStartOfTotalityMills() {
        if (mEclipseTimeManager == null) {
            return null;
        }
        return mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT2);
    }
}
