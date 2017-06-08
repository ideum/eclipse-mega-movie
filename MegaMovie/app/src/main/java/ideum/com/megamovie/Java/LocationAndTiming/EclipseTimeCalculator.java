package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ideum.com.megamovie.R;


public class EclipseTimeCalculator {


    public final static String TAG = "EclipseTimeCalculator";
    EclipseTimingMap mEclipseTimingMap;
    private Context context;
    private LatLng mostRecentLatLng;
    private boolean refreshPending = false;

    private List<EclipseTimingMap.EclipseTimingFile> c1_timing_files = new ArrayList<>();
    private List<EclipseTimingMap.EclipseTimingFile> c2_timing_files = new ArrayList<>();
    private List<EclipseTimingMap.EclipseTimingFile> cm_timing_files = new ArrayList<>();
    private List<EclipseTimingMap.EclipseTimingFile> c3_timing_files = new ArrayList<>();
    private List<EclipseTimingMap.EclipseTimingFile> c4_timing_files = new ArrayList<>();


    public EclipseTimeCalculator(Context context) {
        this.context = context;
        Field[] fields = R.raw.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            String[] parts = name.split("_");

            String contact = parts[0];
            if (!contact.equals("c1")
                    && !contact.equals("c2")
                    && !contact.equals("cm")
                    && !contact.equals("c3")
                    && !contact.equals("c4")) {
                continue;
            }

            int fileId = 0;
            try {
                fileId = fields[i].getInt(fields[i]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            Double startingLng = -Double.valueOf(parts[1]);
            Double endingLng = -Double.valueOf(parts[2]);
            Double startingLat = Double.valueOf(parts[3]);
            Double endingLat = Double.valueOf(parts[4]);
            EclipseTimingMap.EclipseTimingFile etf = new EclipseTimingMap.EclipseTimingFile(fileId, startingLat, endingLat, startingLng, endingLng);


            if (contact.equals("c1")) {
                c1_timing_files.add(etf);
            }
            if (contact.equals("c2")) {
                c2_timing_files.add(etf);
            }
            if (contact.equals("cm")) {
                cm_timing_files.add(etf);
            }
            if (contact.equals("c3")) {
                c3_timing_files.add(etf);
            }
            if (contact.equals("c4")) {
                c4_timing_files.add(etf);
            }
        }

        Log.i("TAG", String.valueOf(c1_timing_files.size()));
    }


    private EclipseTimingMap.EclipseTimingFile getFileForLocation(EclipseTimingMap.Event event, LatLng location) {
        EclipseTimingMap.EclipseTimingFile file = null;

        switch (event) {
            case CONTACT1:
                for (EclipseTimingMap.EclipseTimingFile f : c1_timing_files) {
                    if (f.contains(location)) {
                        file = f;
                    }
                }
                break;

            case CONTACT2:
                for (EclipseTimingMap.EclipseTimingFile f : c2_timing_files) {
                    if (f.contains(location)) {
                        file = f;
                    }
                }
                break;

            case MIDDLE:
                for (EclipseTimingMap.EclipseTimingFile f : cm_timing_files) {
                    if (f.contains(location)) {
                        file = f;
                    }
                }
                break;
            case CONTACT3:
                for (EclipseTimingMap.EclipseTimingFile f : c3_timing_files) {
                    if (f.contains(location)) {
                        file = f;
                    }
                }
                break;
            case CONTACT4:
                for (EclipseTimingMap.EclipseTimingFile f : c4_timing_files) {
                    if (f.contains(location)) {
                        file = f;
                    }
                }
                break;
        }
        return file;
    }

    public Long getEclipseTime(EclipseTimingMap.Event event, LatLng latLng) {
        setMostRecentLatLng(latLng);
        Long result = null;
        if (mEclipseTimingMap != null) {
            result = mEclipseTimingMap.getEclipseTime(event, latLng);
        }
        return result;
    }

    private void setMostRecentLatLng(LatLng latLng) {
        if (refreshPending) {
            return;
        }
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
        EclipseTimingMap.EclipseTimingFile c1File = getFileForLocation(EclipseTimingMap.Event.CONTACT1, latLng);
        EclipseTimingMap.EclipseTimingFile c2File = getFileForLocation(EclipseTimingMap.Event.CONTACT2, latLng);
        EclipseTimingMap.EclipseTimingFile cmFile = getFileForLocation(EclipseTimingMap.Event.MIDDLE, latLng);
        EclipseTimingMap.EclipseTimingFile c3File = getFileForLocation(EclipseTimingMap.Event.CONTACT3, latLng);
        EclipseTimingMap.EclipseTimingFile c4File = getFileForLocation(EclipseTimingMap.Event.CONTACT4, latLng);
        if (c2File != null && c3File != null) {
            refreshPending = true;
            new RefreshTimingMapTask().execute(context, c1File, c2File, cmFile, c3File, c4File);
        }
    }

    private void refreshTimingMap() throws IOException {
        refreshTimingMapWithLatLng(mostRecentLatLng);
    }


    private class RefreshTimingMapTask extends AsyncTask<Object, Void, EclipseTimingMap> {
        @Override
        protected EclipseTimingMap doInBackground(Object... params) {
            Context context = (Context) params[0];
            EclipseTimingMap.EclipseTimingFile c1_etf = (EclipseTimingMap.EclipseTimingFile) params[1];
            EclipseTimingMap.EclipseTimingFile c2_etf = (EclipseTimingMap.EclipseTimingFile) params[2];
            EclipseTimingMap.EclipseTimingFile cm_etf = (EclipseTimingMap.EclipseTimingFile) params[3];
            EclipseTimingMap.EclipseTimingFile c3_etf = (EclipseTimingMap.EclipseTimingFile) params[4];
            EclipseTimingMap.EclipseTimingFile c4_etf = (EclipseTimingMap.EclipseTimingFile) params[5];

            EclipseTimingMap etm = null;

            try {
                etm = new EclipseTimingMap(context,c1_etf, c2_etf, cm_etf, c3_etf, c4_etf);
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
