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
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.megamovie.R;

/**
 * Created by MT_User on 6/2/2017.
 */

public class EclipseTimeLocationManager implements LocationSource.OnLocationChangedListener {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    private LatLng currentLatLng;
    private LatLng currentClosestTotalityLatLng;
    private LatLng plannedLatLng;
    private Context mContext;



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

    private void setCurrentClosestTotalityLatLng(LatLng latLng) {

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
        currentClosestTotalityLatLng = EclipsePath.closestPointOnPathOfTotality(currentLatLng);
    }



    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setCurrentLatLng(latLng);
    }

    private LatLng referenceLatLng() {
        LatLng plannedLatLng = getPlannedLatLngPreference();
        if (plannedLatLng != null) {
            return plannedLatLng;
        }

        return currentClosestTotalityLatLng;

    }

    private String getTimeZoneId() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString(mContext.getString(R.string.timezone_id),"");
    }

    public String getContactTimeString(EclipseTimingMap.Event event) {


        Long mills = getEclipseTime(event);
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
            timeZoneDisplayName = TimeZone.getTimeZone(timeZoneId).getDisplayName(true,TimeZone.SHORT,Locale.US);
        }


        return formatter.format(calendar.getTime()) + " " + timeZoneDisplayName;


    }


}
