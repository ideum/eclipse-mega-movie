package ideum.com.megamovie.Java.PatagoniaTest;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;

import ideum.com.megamovie.Java.Utility.EclipsePath;
import ideum.com.megamovie.Java.Utility.EclipseTimingMap;
import ideum.com.megamovie.R;


public class EclipseTimeCalculator
        implements LocationListener {

    public final static String TAG = "EclipseTimeCalculator";

    EclipseTimingMap mEclipseTimingMap;
    private Context context;
    private Location currentLocation;

    private final EclipseTimingMap.EclipseTimingFile[] c2_timing_files = {
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t125119_4346, 43.0, 46.0, -125.0, -119.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t119114_4346, 43.0, 46.0, -119.0, -114.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t114109_4245, 42.0, 45.0, -114.0, -109.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t109105_4144, 41.0, 44.0, -109.0, -105.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t105101_4044, 40.0, 44.0, -105.0, -101.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t101097_3943, 39.0, 43.0, -101.0, -97.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t097093_3842, 38.0, 42.0, -97.0, -93.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t093090_3640, 36.0, 40.0, -93.0, -90.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c2_t090085_3539, 35.0, 39.0, -90.0, -85.0)
    };

    private final EclipseTimingMap.EclipseTimingFile[] c3_timing_files = {
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t087084_3438, 34.0, 38.0, -87.0, -84.0)
    };

    private EclipseTimingMap.EclipseTimingFile getC2FileForLocation(Location location) {
        LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
        return getC2FileForLocation(loc);
    }


    private EclipseTimingMap.EclipseTimingFile getC2FileForLocation(LatLng location) {
        EclipseTimingMap.EclipseTimingFile file = null;

        for (EclipseTimingMap.EclipseTimingFile f : c2_timing_files) {
            if (f.contains(location)) {
                file = f;
            }
        }
        return file;
    }

    public EclipseTimeCalculator(Context context) {
        this.context = context;
    }

    public Long getEclipseTime(EclipseTimingMap.Event event) {
        if (currentLocation == null || mEclipseTimingMap == null) {
            return null;
        }
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng closestEclipseLatLng = EclipsePath.closestPointOnPathOfTotality(latLng);

        return mEclipseTimingMap.getEclipseTime(event, closestEclipseLatLng);
    }

    public Long getTimeToEvent(EclipseTimingMap.Event event) {

        if (currentLocation == null) {
            return null;
        }

        Long eventTime = getEclipseTime(event);
        if (eventTime == null) {
            return null;
        }
        return eventTime - getCurrentTime();
    }

    private long getCurrentTime() {
        Calendar calendar = Calendar.getInstance();

        return calendar.getTimeInMillis();
    }

    private boolean currentMapContainsLocation(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng loc = new LatLng(lat, lng);
        return currentMapContainsLocation(loc);
    }

    private boolean currentMapContainsLocation(LatLng location) {
        if (mEclipseTimingMap == null) {
            return false;
        }
        return mEclipseTimingMap.containsLocation(location);
    }

    private void refreshTimingMap() throws IOException {

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                EclipseTimingMap.EclipseTimingFile etf = getC2FileForLocation(currentLocation);
                try {
                    mEclipseTimingMap = new EclipseTimingMap(context, etf, c3_timing_files[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setLocation(Location location) {
        currentLocation = location;

        if (!currentMapContainsLocation(location)) {
            try {
                refreshTimingMap();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
    }
}
