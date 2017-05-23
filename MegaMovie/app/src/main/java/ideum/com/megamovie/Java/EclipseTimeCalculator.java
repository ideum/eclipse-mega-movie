package ideum.com.megamovie.Java;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import ideum.com.megamovie.Java.Utility.EclipsePath;
import ideum.com.megamovie.Java.Utility.EclipseTimingMap;
import ideum.com.megamovie.R;


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
