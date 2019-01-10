package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import ideum.com.megamovie.Java.Application.Config;

/**
 * Created by MT_User on 5/10/2017.
 * Stores a dictionary of eclipse times for a collection of gps coordinates in a given region. These values
 * are parsed from a text file with integers arranged in rows and columns delimited by spaces and newlines, which
 * correspond to time offsets from a base time.
 */

public class EclipseTimingMap {
    /*Used to convert a row-column position in the text file to a latlng coordinate */
    private static final Double LATLNG_INTERVAL = 0.01;
    /*Used to convert integers read from raw text file to milliseconds */
    private static final long MILLISECONDS_PER_TIME_UNIT = 100;
    private EclipseTimingFile c1EclipseTimingFile;
    private EclipseTimingFile c2EclipseTimingFile;
    private EclipseTimingFile cmEclipseTimingFile;
    private EclipseTimingFile c3EclipseTimingFile;
    private EclipseTimingFile c4EclipseTimingFile;

    public Map<MyKey, Double> eclipseTimeMapC1;
    public Map<MyKey, Double> eclipseTimeMapC2;
    public Map<MyKey, Double> eclipseTimeMapCm;
    public Map<MyKey, Double> eclipseTimeMapC3;
    public Map<MyKey, Double> eclipseTimeMapC4;

    private Context context;

    public List<Integer> getIntList() {
        List<Integer> ints = new ArrayList<>();

        for(MyKey key : eclipseTimeMapC1.keySet()) {
            ints.add((int)Math.round(eclipseTimeMapC1.get(key)));
        }

        return ints;
    }

    public EclipseTimingMap(Context context,
                            EclipseTimingFile c1File,
                            EclipseTimingFile c2File,
                            EclipseTimingFile cmFile,
                            EclipseTimingFile c3File,
                            EclipseTimingFile c4File) throws IOException {
        this.context = context;

        c1EclipseTimingFile = c1File;
        c2EclipseTimingFile = c2File;
        cmEclipseTimingFile = cmFile;
        c3EclipseTimingFile = c3File;
        c4EclipseTimingFile = c4File;

        eclipseTimeMapC1 = parseTextFile(context, c1EclipseTimingFile);
        eclipseTimeMapC2 = parseTextFile(context, c2EclipseTimingFile);
        eclipseTimeMapCm = parseTextFile(context, cmEclipseTimingFile);
        eclipseTimeMapC3 = parseTextFile(context, c3EclipseTimingFile);
        eclipseTimeMapC4 = parseTextFile(context, c4EclipseTimingFile);
    }


    public boolean containsLocation(LatLng location) {
        if (c1EclipseTimingFile == null
                || c2EclipseTimingFile == null
                || cmEclipseTimingFile == null
                || c3EclipseTimingFile == null
                || c4EclipseTimingFile == null) {
            return false;
        }
        return c1EclipseTimingFile.contains(location)
                && c2EclipseTimingFile.contains(location)
                && cmEclipseTimingFile.contains(location)
                && c3EclipseTimingFile.contains(location)
                && c4EclipseTimingFile.contains(location);
    }

    public Long getEclipseTime(EclipseTimingMap.Event event, LatLng location) {

        if (eclipseTimeMapC2 == null || cmEclipseTimingFile == null || eclipseTimeMapC3 == null || c4EclipseTimingFile == null) {
            return null;
        }

        double lat = location.latitude;
        double lng = location.longitude;

        int x = (int) (lat / LATLNG_INTERVAL);
        int y = (int) (lng / LATLNG_INTERVAL);

        MyKey key = new MyKey(x, y);
        Double timeUnitsAfterBaseTime = 0.0;
        switch (event) {
            case CONTACT1:
                timeUnitsAfterBaseTime = eclipseTimeMapC1.get(key);
                if (timeUnitsAfterBaseTime == null || timeUnitsAfterBaseTime == 0) {
                    return null;
                }
                break;
            case CONTACT2:
                timeUnitsAfterBaseTime = eclipseTimeMapC2.get(key);
                if (timeUnitsAfterBaseTime == null || timeUnitsAfterBaseTime == 0) {
                    return null;
                }
                break;

            case MIDDLE:
                timeUnitsAfterBaseTime = eclipseTimeMapCm.get(key);
                if (timeUnitsAfterBaseTime == null || timeUnitsAfterBaseTime == 0) {
                    return null;
                }
                break;

            case CONTACT3:
                timeUnitsAfterBaseTime = eclipseTimeMapC3.get(key);
                if (timeUnitsAfterBaseTime == null || timeUnitsAfterBaseTime == 0) {
                    return null;
                }
                break;
            default:
                return null;

            case CONTACT4:
                timeUnitsAfterBaseTime = eclipseTimeMapC4.get(key);
                if (timeUnitsAfterBaseTime == null || timeUnitsAfterBaseTime == 0) {
                    return null;
                }
                break;
        }

        long millsAfterBaseTime = MILLISECONDS_PER_TIME_UNIT * Math.round(timeUnitsAfterBaseTime);
        return getBaseTime() + millsAfterBaseTime;
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


    private class MyKey {
        final int x;
        final int y;

        public MyKey(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MyKey)) {
                return false;
            }
            MyKey key = (MyKey) o;
            return (x == key.x) && (y == key.y);
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }

    private Map<MyKey, Double> parseTextFile(Context context, EclipseTimingFile file) throws IOException {
        return parseTextFile(context, file.fileId, file.startingLat, file.startingLng);
    }

    private Map<MyKey, Double> parseTextFile(Context context, int resource_file_id, Double startingLat, Double startingLng) throws IOException {
        int startingLatInt = (int) (startingLat / LATLNG_INTERVAL);
        int startingLngInt = (int) (startingLng / LATLNG_INTERVAL);

        InputStream inputStream = context.getResources().openRawResource(resource_file_id);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = br.readLine();
        int lineLength = line.length();
        int x = startingLatInt;
        Map<MyKey, Double> map = new HashMap<>();

        while (line != null && line.length() == lineLength) {
            ArrayList<Double> times = parseLine(line);
            int y = startingLngInt;
            for (Double t : times) {
                map.put(new MyKey(x, y), t);
                y++;
            }
            x++;
            line = br.readLine();
        }
        return map;
    }


    private ArrayList<Double> parseLine(String line) {
        String[] separated = line.split(" ");
        ArrayList<Double> result = new ArrayList<>();
        for (String s : separated) {
            if (!s.equals("")) {
                result.add(Double.parseDouble(s));
            }
        }
        return result;
    }


    public enum Event {
        CONTACT1, CONTACT2, MIDDLE, CONTACT3, CONTACT4
    }

    public static class EclipseTimingFile {
        public int fileId;
        public double startingLat;
        public double endingLat;
        public double startingLng;
        public double endingLng;

        public EclipseTimingFile(int fileID, double startingLat, double endingLat, double startingLng, double endingLng) {
            this.fileId = fileID;
            this.startingLat = startingLat;
            this.endingLat = endingLat;
            this.startingLng = startingLng;
            this.endingLng = endingLng;
        }

        public boolean contains(LatLng location) {
            double lat = location.latitude;
            double lng = location.longitude;
            return lat >= startingLat
                    && lat <= endingLat
                    && lng >= startingLng
                    && lng <= endingLng;

        }
    }

}
