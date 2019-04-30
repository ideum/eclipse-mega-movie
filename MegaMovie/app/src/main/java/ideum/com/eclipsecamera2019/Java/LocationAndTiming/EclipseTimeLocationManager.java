package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;


import java.util.Calendar;
import java.util.Date;

import ideum.com.eclipsecamera2019.R;

import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c1;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c3;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c4;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.cm;

/**
 * Provides the time of eclipse phases based on a stored location, which can either be the user's current
 * location provided by gps, or an arbitrary location selected on a map. If the location is not in the
 * path of totality, the closest point in the path is used to calculate the eclipse time.
 */

public class EclipseTimeLocationManager implements LocationSource.OnLocationChangedListener {

    private EclipseTimes mEclipseTimes;
    private LatLng currentLatLng;
    private LatLng currentClosestTotalityLatLng;
    private Context mContext;
    public boolean shouldUseCurrentLocation = false;
    private Long timeCorrection = 0L;

    public EclipseTimeLocationManager(EclipseTimes eclipseTimes, Context context) {
        mContext = context;
        this.mEclipseTimes = eclipseTimes;
    }

    public Long getEclipseTime(EclipseTimes.Phase phase) {
        if (referenceLatLng() == null || mEclipseTimes == null) {
            return null;
        }

        return mEclipseTimes.getEclipseTime(phase, referenceLatLng());//  mEclipseTimeCalculator.getEclipseTime(event, referenceLatLng());
    }

    public Long[] getEclipseTimes() {
        Long[] times = new Long[5];
        times[0] = mEclipseTimes.getEclipseTime(c1,referenceLatLng());
        times[1] = mEclipseTimes.getEclipseTime(c2,referenceLatLng());
        times[2] = mEclipseTimes.getEclipseTime(cm,referenceLatLng());
        times[3] = mEclipseTimes.getEclipseTime(c3,referenceLatLng());
        times[4] = mEclipseTimes.getEclipseTime(c4,referenceLatLng());

        return times;
    }

    public Long getTimeToEclipse(EclipseTimes.Phase phase) {
        if (mEclipseTimes == null) {
            return null;
        }
        Long result = null;
        Long eclipseTime = getEclipseTime(phase);
        if (eclipseTime != null) {
            result = eclipseTime - getCurrentCalibrateTimeMills();
        }
        return result;
    }

    public void calibrateTime(Long correctTime) {
        Long systemTime = Calendar.getInstance().getTimeInMillis();
        Date date = new Date(systemTime);
        timeCorrection = correctTime - systemTime;
    }

    private Long getCurrentCalibrateTimeMills() {
        return Calendar.getInstance().getTimeInMillis();// + timeCorrection;
    }

    public void setAsLocationListener(LocationSource source) {
        source.activate(this);
    }

    public void setCurrentLatLng(LatLng latLng) {
        currentLatLng = latLng;
        currentClosestTotalityLatLng = EclipsePath.closestPointOnPathOfTotality(currentLatLng);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setCurrentLatLng(latLng);
    }

    public LatLng referenceLatLng() {
        LatLng plannedLatLng = getPlannedLatLngPreference();
        if (plannedLatLng != null && !shouldUseCurrentLocation) {
            return plannedLatLng;
        }

        return currentClosestTotalityLatLng;
    }

    private LatLng getPlannedLatLngPreference() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        float lat = settings.getFloat(mContext.getString(R.string.planned_lat_key), 0);
        float lng = settings.getFloat(mContext.getString(R.string.planned_lng_key), 0);
        LatLng result = null;
        if (lat != 0 && lng != 0) {
            result = new LatLng(lat, lng);
        }
        return result;
    }


}
