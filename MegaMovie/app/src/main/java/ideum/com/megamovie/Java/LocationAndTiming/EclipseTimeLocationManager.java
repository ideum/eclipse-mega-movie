package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

import ideum.com.megamovie.R;

/**
 * Created by MT_User on 6/2/2017.
 */

public class EclipseTimeLocationManager implements LocationSource.OnLocationChangedListener {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    private LatLng currentLatLng;
    private Context mContext;


    private LatLng getPlannedLatLng() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        float lat = settings.getFloat(mContext.getString(R.string.planned_lat_key),0);
        float lng = settings.getFloat(mContext.getString(R.string.planned_lng_key),0);
        LatLng result;
        if (lat != 0 && lng != 0) {
            result = new LatLng(lat,lng);
        } else {
            result = null;
        }
        return result;
    }


    public EclipseTimeLocationManager(EclipseTimeCalculator etc, Context context) {
        mEclipseTimeCalculator = etc;
        mContext = context;
    }

    public Long getEclipseTime(EclipseTimingMap.Event event) {
        if (referenceLatLng() == null) {
            return null;
        }
        return mEclipseTimeCalculator.getEclipseTime(event, referenceLatLng());
    }

    public Long getTimeToEclipse(EclipseTimingMap.Event event) {
        Long result = null;
        Long eclipseTime = getEclipseTime(event);
        if (eclipseTime != null) {
            result = eclipseTime - Calendar.getInstance().getTimeInMillis();
        }
        return result;
    }

    public void setAsLocationListener(LocationSource source) {
        source.activate(this);
    }

    public void setCurrentLatLng(LatLng latLng) {
        currentLatLng = latLng;
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            setCurrentLatLng(latLng);
    }

    private LatLng referenceLatLng() {
        LatLng plannedLatLng = getPlannedLatLng();
        if (plannedLatLng != null) {
            return plannedLatLng;
        }

        if (currentLatLng == null) {
            return null;
        }

        return  EclipsePath.closestPointOnPathOfTotality(currentLatLng);
    }
}
