package ideum.com.megamovie.Java;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ideum.com.megamovie.R;


public class EclipseTimeCalculator {
    public final static String TAG = "EclipseTimeCalculator";
    private static final Double STARTING_LAT = 44.5;
    private static final Double STARTING_LNG = 66.0;
    private static final Double LATLNG_INTERVAL = 0.01;
    private Map<MyKey,Double> eclipseTimeMap;
    private final static int TIMING_FILE_RESOURCE_ID = R.raw.c2_times;

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
        CONTACT4;
    }

    public EclipseTimeCalculator(Context context) throws IOException{
        eclipseTimeMap = parseTextFile(context);
    }
    public Double getEclipseTime(LatLng location) {
        double lat = location.latitude;
        double lng = location.longitude;
        int x = (int)((lat - STARTING_LAT)/LATLNG_INTERVAL);
        int y = (int)((lng - STARTING_LNG)/LATLNG_INTERVAL);
        MyKey key = new MyKey(x,y);
        Double time = eclipseTimeMap.get(key);
        if (time == -1) {
            return null;
        }
        return time;
    }

    // This method is just a stand-in for now
    public long dummyEclipseTime(Event event, LatLng location) {
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH,7);
        calendar.set(Calendar.DAY_OF_MONTH,21);
        calendar.set(Calendar.HOUR,5);
        calendar.set(Calendar.MINUTE,10);
        calendar.set(Calendar.SECOND, 40);

        long startTime = calendar.getTimeInMillis();
        long contactTime = 0;

        switch (event) {
            case CONTACT1:
                contactTime = startTime;
                break;
            case CONTACT2:
                contactTime = startTime + 10000;
                break;
            case CONTACT2_END:
                contactTime = startTime + 10000;
                break;
            case CONTACT3:
                contactTime = startTime + 10000;
                break;
            case CONTACT3_END:
                contactTime = startTime + 10000;
                break;
            case CONTACT4:
                contactTime = startTime + 10000;
                break;
        }
        return contactTime;
    }

    private Map<MyKey, Double> parseTextFile(Context context) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(TIMING_FILE_RESOURCE_ID);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = br.readLine();
        int lineLength = line.length();
        int y = 0;
        Map<MyKey, Double> map = new HashMap<>();

        while (line != null && line.length() == lineLength) {
            ArrayList<Double> times = parseLine(line);
            int x = 0;
            for (Double t : times) {
                map.put(new MyKey(x, y), t);
                x++;
            }
            y++;
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

}
