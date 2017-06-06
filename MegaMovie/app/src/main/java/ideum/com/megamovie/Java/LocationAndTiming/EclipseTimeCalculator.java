package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import ideum.com.megamovie.R;


public class EclipseTimeCalculator {


    public final static String TAG = "EclipseTimeCalculator";
    EclipseTimingMap mEclipseTimingMap;
    private Context context;
    private LatLng mostRecentLatLng;
    private boolean refreshPending = false;

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
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t125119_4346, 43.0, 46.0, -125.0, -119.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t119114_4346, 43.0, 46.0, -119.0, -114.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t114109_4245, 42.0, 45.0, -114.0, -109.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t109105_4144, 41.0, 44.0, -109.0, -105.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t105101_4044, 40.0, 44.0, -105.0, -101.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t101097_3943, 39.0, 43.0, -101.0, -97.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t097093_3842, 38.0, 42.0, -97.0, -93.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t093090_3640, 36.0, 40.0, -93.0, -90.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t090087_3539, 35.0, 39.0, -90.0, -87.0),
            new EclipseTimingMap.EclipseTimingFile(R.raw.c3_t087084_3438, 34.0, 38.0, -87.0, -84.0)
    };


    public EclipseTimeCalculator(Context context) {
        this.context = context;
    }

    private EclipseTimingMap.EclipseTimingFile getFileForLocation(EclipseTimingMap.Event event, LatLng location) {
        EclipseTimingMap.EclipseTimingFile file = null;

        switch (event) {
            case CONTACT2:
            for (EclipseTimingMap.EclipseTimingFile f : c2_timing_files) {
                if (f.contains(location)) {
                    file = f;
                }
            } break;
            case CONTACT3:
                for (EclipseTimingMap.EclipseTimingFile f : c3_timing_files) {
                    if (f.contains(location)) {
                        file = f;
                    }
                } break;
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
        if (refreshPending) {
            return;
        }
        mostRecentLatLng = latLng;

        if (!currentMapContainsLocation(mostRecentLatLng)) {
            Log.i("TAG","refreshing map");
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
        EclipseTimingMap.EclipseTimingFile c2File = getFileForLocation(EclipseTimingMap.Event.CONTACT2, latLng);
        EclipseTimingMap.EclipseTimingFile c3File =  getFileForLocation(EclipseTimingMap.Event.CONTACT3, latLng);
        if (c2File != null && c3File != null) {
            refreshPending = true;
            new RefreshTimingMapTask().execute(context, c2File, c3File);
        }
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
            refreshPending = false;
        }
    }
}
