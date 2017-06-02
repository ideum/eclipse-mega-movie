package ideum.com.megamovie.Java.LocationAndTiming;

import android.location.Location;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

/**
 * Created by MT_User on 6/2/2017.
 */

public class EclipseTimeManager implements LocationListener {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    private LatLng currentLatLng;

    public EclipseTimeManager(EclipseTimeCalculator etc) {
        mEclipseTimeCalculator = etc;
    }

    public Long getEclipseTimeC2() {
        if (currentLatLng == null) {
            return null;
        }
        return mEclipseTimeCalculator.getEclipseTime(EclipseTimingMap.Event.CONTACT2,currentLatLng);
    }

    public Long getTimeToEclipse(EclipseTimingMap.Event event) {
        Long result = null;
        if (event == EclipseTimingMap.Event.CONTACT2) {
            Long c2Time = getEclipseTimeC2();
            if (c2Time != null) {
                Calendar c = Calendar.getInstance();
                result = c2Time - c.getTimeInMillis();
            }
        }
        return result;
    }

    public void setAsLocationListener(LocationNotifier notifier) {
        notifier.addLocationListener(this);
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLatLng = EclipsePath.closestPointOnPathOfTotality(latLng);
    }
}
