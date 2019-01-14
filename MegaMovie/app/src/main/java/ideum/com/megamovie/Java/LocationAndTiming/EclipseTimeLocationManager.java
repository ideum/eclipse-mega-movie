package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;


import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.megamovie.R;

/**
 * Provides the time of eclipse phases based on a stored location, which can either be the user's current
 * location provided by gps, or an arbitrary location selected on a map. If the location is not in the
 * path of totality, the closest point in the path is used to calculate the eclipse time.
 */

public class EclipseTimeLocationManager implements LocationSource.OnLocationChangedListener {

    //private EclipseTimeCalculator mEclipseTimeCalculator;
    private EclipseTimes mEclipseTimes;
    private LatLng currentLatLng;
    private LatLng currentClosestTotalityLatLng;
    private Context mContext;
    public boolean shouldUseCurrentLocation = false;
    private Long timeCorrection = 0L;

    public EclipseTimeLocationManager(EclipseTimes eclipseTimes, Context context) {
        //mEclipseTimeCalculator = etc;
        mContext = context;
        this.mEclipseTimes = eclipseTimes;
    }

    public Long getEclipseTime(EclipseTimes.Phase phase) {
        if (referenceLatLng() == null || mEclipseTimes == null) {
            return null;
        }

        return mEclipseTimes.getEclipseTime(phase, referenceLatLng());//  mEclipseTimeCalculator.getEclipseTime(event, referenceLatLng());
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

    private String getTimeZoneId() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(mContext.getString(R.string.timezone_id), "");
    }

    public String getContactTimeString(EclipseTimes.Phase phase) {
        if (mEclipseTimes == null) {
            return "";
        }
        Long mills = getEclipseTime(phase);
        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String timeZoneId = getTimeZoneId();
        String timeZoneDisplayName = "";
        if (timeZoneId != null) {
            formatter.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            timeZoneDisplayName = TimeZone.getTimeZone(timeZoneId).getDisplayName(true, TimeZone.SHORT, Locale.US);
        }


        return formatter.format(calendar.getTime()) + " " + timeZoneDisplayName;
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
