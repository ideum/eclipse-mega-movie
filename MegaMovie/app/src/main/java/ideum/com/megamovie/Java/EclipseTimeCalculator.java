package ideum.com.megamovie.Java;

import android.content.Context;
import android.location.Location;
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

import ideum.com.megamovie.R;


public class EclipseTimeCalculator {
    public final static String TAG = "EclipseTimeCalculator";
    private static final Double STARTING_LAT = -44.5;
    private static final Double STARTING_LNG = -66.0;
    private static final Double LATLNG_INTERVAL = -0.01;
    private Map<MyKey, Double> eclipseTimeMapC2;
    private Map<MyKey, Double> eclipseTimeMapC3;
    private final static int C2_TIMING_FILE_RESOURCE_ID = R.raw.c2_times;
    private final static int C3_TIMING_FILE_RESOURCE_ID = R.raw.c3_times;
    private LocationProvider mLocationProvider;

    private final static int BASETIME_YEAR = 2017;
    private final static int BASETIME_MONTH = 01;
    private final static int BASETIME_DAY = 26;
    private final static int BASETIME_HOUR = 13;

    private final static boolean USE_DUMMY_ECLIPSE_TIME = true;

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

    public enum Event {
        CONTACT1,
        CONTACT2, CONTACT2_END,
        CONTACT3, CONTACT3_END,
        CONTACT4
    }

    public EclipseTimeCalculator(Context context, LocationProvider provider) throws IOException {
        eclipseTimeMapC2 = parseTextFile(context, C2_TIMING_FILE_RESOURCE_ID);
        eclipseTimeMapC3 = parseTextFile(context, C3_TIMING_FILE_RESOURCE_ID);
        mLocationProvider = provider;
    }

    public Long getEclipseTime(Location location, Event event) {
//        if (mLocationProvider == null) {
//            return null;
//        }
//        Location location = mLocationProvider.getLocation();
//        if (location == null) {
//            return null;
//        }

        if (USE_DUMMY_ECLIPSE_TIME) {
            return dummyEclipseTime(event);
        }
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        int x = (int) ((lat - STARTING_LAT) / LATLNG_INTERVAL);
        int y = (int) ((lng - STARTING_LNG) / LATLNG_INTERVAL);
        MyKey key = new MyKey(x, y);
        Double secondsAfterBaseTime = 0.0;
        switch (event) {
            case CONTACT2:
                secondsAfterBaseTime = eclipseTimeMapC2.get(key);
                if (secondsAfterBaseTime == null || secondsAfterBaseTime == 0) {
                    return null;
                }
                break;
            case CONTACT3:
                secondsAfterBaseTime = eclipseTimeMapC3.get(key);
                if (secondsAfterBaseTime == null || secondsAfterBaseTime == 0) {
                    return null;
                }
                break;
            default:
                return null;
        }

        long millsAfterBaseTime = 1000 * Math.round(secondsAfterBaseTime);

        return getBaseTime() + millsAfterBaseTime;
    }

    private long getBaseTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, BASETIME_YEAR);
        calendar.set(Calendar.MONTH, BASETIME_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, BASETIME_DAY);
        calendar.set(Calendar.HOUR, BASETIME_HOUR);
        return calendar.getTimeInMillis();
    }

    // This method is just a stand-in for now
    public long dummyEclipseTime(Event event) {
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH,7);
//        calendar.set(Calendar.DAY_OF_MONTH,11);
//        calendar.set(Calendar.HOUR,5);

        calendar.set(Calendar.MINUTE,45);
        calendar.set(Calendar.SECOND, 15);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long contactTime = 0;

        switch (event) {
            case CONTACT1:
                contactTime = startTime;
                break;
            case CONTACT2:
                contactTime = startTime;
                break;
            case CONTACT2_END:
                contactTime = startTime;
                break;
            case CONTACT3:
                contactTime = startTime + 60000;
                break;
            case CONTACT3_END:
                contactTime = startTime + 60000;
                break;
            case CONTACT4:
                contactTime = startTime + 75000;
                break;
        }
        return contactTime;
    }

    private Map<MyKey, Double> parseTextFile(Context context, int resource_file_id) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(resource_file_id);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = br.readLine();
        int lineLength = line.length();
        int x = 0;
        Map<MyKey, Double> map = new HashMap<>();

        while (line != null && line.length() == lineLength) {
            ArrayList<Double> times = parseLine(line);
            int y = 0;
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

    private String timeOfDayString(Long mills) {
        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);

        return formatter.format(calendar.getTime());
    }
}
