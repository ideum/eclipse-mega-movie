package ideum.com.megamovie.Java.LocationAndTiming;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.TimeZone;

import ideum.com.megamovie.Java.Application.Config;

public class EclipseTimingPatch {


    private double latMin;
    private double latMax;
    private double lngMin;
    private double lngMax;
    private double latLngInterval;
    private int[] timeOffsets;
    private static final int MILLSEC_PER_TIME_UNIT = 100;


    private int numCols() {
        return (int)((lngMax - lngMin)/latLngInterval) + 1;
    }

    public boolean contains(LatLng p) {
        double lat = p.latitude;
        double lng = p.longitude;
        return lat >= latMin && lat <= latMax && lng >= lngMin && lng <= lngMax;
    }

    public Long getEclipseTimeMills(LatLng p) {
        int row = (int)((p.latitude - latMin)/latLngInterval);
        int col = (int)((p.longitude - lngMin)/latLngInterval);
        int index = row * numCols() + col;
        if (index >= 0 && index < timeOffsets.length) {
            return getBaseTime() + MILLSEC_PER_TIME_UNIT * timeOffsets[index];
        } else {
            return null;
        }
    }

    public EclipseTimingPatch(double latMin, double latMax, double lngMin, double lngMax, double latLngInterval, int[] timeOffsets) {
        this.latMin = latMin;
        this.latMax = latMax;
        this.lngMin  = lngMax;
        this.lngMax = lngMax;
        this.latLngInterval = latLngInterval;
        this.timeOffsets = timeOffsets;
    }

    private long getBaseTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, Config.ECLIPSE_BASETIME_YEAR);
        calendar.set(Calendar.MONTH, Config.ECLIPSE_BASETIME_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, Config.ECLIPSE_BASETIME_DAY);
        calendar.set(Calendar.HOUR, Config.ECLIPSE_BASETIME_HOUR);
        calendar.set(Calendar.MINUTE, Config.ECLIPSE_BASETIME_MINUTE);
        return calendar.getTimeInMillis();
    }
}
