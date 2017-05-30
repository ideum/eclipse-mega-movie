package ideum.com.megamovie.Java.PatagoniaTest;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import ideum.com.megamovie.Java.Utility.EclipsePath;
import ideum.com.megamovie.Java.Utility.EclipseTimingMap;


public class EclipseTimeCalculator {
    public final static String TAG = "EclipseTimeCalculator";

    LocationProvider mLocationProvider;

    EclipseTimingMap mEclipseTimingMap;

    public EclipseTimeCalculator(EclipseTimingMap etm, LocationProvider provider) {
        mEclipseTimingMap = etm;
        mLocationProvider = provider;
    }
    public EclipseTimeCalculator(Context context, LocationProvider provider) throws IOException {

        mEclipseTimingMap = new EclipseTimingMap(context);
        mLocationProvider = provider;
    }

    public Long getEclipseTime(EclipseTimingMap.Event event) {
        if (mLocationProvider == null) {
            return null;
        }
        Location location = mLocationProvider.getLocation();
        if (location == null) {
            return null;
        }

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        LatLng closestEclipseLatLng = EclipsePath.closestPointOnPathOfTotality(latLng);

        return mEclipseTimingMap.getEclipseTime(event,closestEclipseLatLng);


    }

    public Long getTimeToEvent(EclipseTimingMap.Event event) {

        if (mLocationProvider == null) {
            return null;
        }
        Location location = mLocationProvider.getLocation();

        if (location == null) {
            return null;
        }

        Long eventTime = getEclipseTime(event);
        if (eventTime == null) {
            return null;
        }
        return eventTime - location.getTime();
    }
}
