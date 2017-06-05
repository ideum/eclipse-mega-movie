package ideum.com.megamovie.Java.LocationAndTiming;

import android.location.Location;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

/**
 * Created by MT_User on 6/2/2017.
 */

public class EclipseTimeLocationManager implements LocationSource.OnLocationChangedListener {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    private LatLng currentLatLng;
    private LatLng cloestTotalityLatLng() {
        if (currentLatLng == null) {
            return null;
        }
        return EclipsePath.closestPointOnPathOfTotality(currentLatLng);
    }

    public boolean shouldUpdateLocationFromGPS = true;


    public EclipseTimeLocationManager(EclipseTimeCalculator etc) {
        mEclipseTimeCalculator = etc;
    }

    public Long getEclipseTime(EclipseTimingMap.Event event) {
        if (currentLatLng == null) {
            return null;
        }
        return mEclipseTimeCalculator.getEclipseTime(event,cloestTotalityLatLng());
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
        if (shouldUpdateLocationFromGPS) {
            setCurrentLatLng(latLng);
        }
    }
}
