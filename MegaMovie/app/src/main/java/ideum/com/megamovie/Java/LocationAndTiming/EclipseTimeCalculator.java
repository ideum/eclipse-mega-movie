package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;

import ideum.com.megamovie.Java.LocationAndTiming.EclipsePath;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.R;


public class EclipseTimeCalculator {


    public final static String TAG = "EclipseTimeCalculator";
    EclipseTimingMap mEclipseTimingMap;
    private Context context;
    private LatLng mostRecentLatLng;

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


    public EclipseTimeCalculator(Context context) {
        this.context = context;
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

    public Long getEclipseTime(EclipseTimingMap.Event event,LatLng latLng) {
        setMostRecentLatLng(latLng);
        Long result = null;
        if (mEclipseTimingMap != null) {
            result = mEclipseTimingMap.getEclipseTime(event,latLng);
        }
        return result;
    }

    private void setMostRecentLatLng(LatLng latLng) {
        mostRecentLatLng = latLng;
        if (!currentMapContainsLocation(mostRecentLatLng)) {
            try {
                refreshTimingMap();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean currentMapContainsLocation(LatLng location) {
        if (mEclipseTimingMap == null) {
            return false;
        }
        return mEclipseTimingMap.containsLocation(location);
    }

    private void refreshTimingMapWithLatLng(LatLng latLng) throws IOException {
        EclipseTimingMap.EclipseTimingFile etf = getC2FileForLocation(latLng);
        new RefreshTimingMapTask().execute(context, etf, c3_timing_files[0]);
    }

    private void refreshTimingMap() throws IOException {
        refreshTimingMapWithLatLng(mostRecentLatLng);
    }


    private class RefreshTimingMapTask extends AsyncTask<Object, Void, EclipseTimingMap> {
        @Override
        protected EclipseTimingMap doInBackground(Object... params) {
            Context context = (Context) params[0];
            EclipseTimingMap.EclipseTimingFile c2_etf = (EclipseTimingMap.EclipseTimingFile) params[1];
            EclipseTimingMap.EclipseTimingFile c3_etf = (EclipseTimingMap.EclipseTimingFile) params[2];

            EclipseTimingMap etm = null;

            try {
                etm = new EclipseTimingMap(context, c2_etf, c3_etf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return etm;
        }

        @Override
        protected void onPostExecute(EclipseTimingMap eclipseTimingMap) {
            super.onPostExecute(eclipseTimingMap);
            mEclipseTimingMap = eclipseTimingMap;
        }
    }
}
