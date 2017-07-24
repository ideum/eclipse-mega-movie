/**
 * Encodes the data for a sequence of camera captures.
 */

package ideum.com.megamovie.Java.CameraControl;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * Classes for specifying sequence of camera captures. A sequence consists of a collection of intervals,
 * during which the camera settings and frequency of captures are fixed.
 */

public class CaptureSequence {
    public static class CaptureSettings {
            public long exposureTime;
            public int sensitivity;
            public float focusDistance;
            public boolean shouldSaveRaw;
            public boolean shouldSaveJpeg;
            public static final String TAG = "CaptureSequence";

        public CaptureSettings(long exposureTime, int sensitivity, float focusDistance, boolean shouldSaveRaw, boolean shouldSaveJpeg) {
            this.exposureTime = exposureTime;
            this.sensitivity = sensitivity;
            this.focusDistance = focusDistance;
            this.shouldSaveJpeg = shouldSaveJpeg;
            this.shouldSaveRaw = shouldSaveRaw;
        }

        public CaptureSettings(IntervalProperties properties) {
            this.exposureTime = properties.sensorExposureTime;
            this.sensitivity = properties.sensorSensitivity;
            this.focusDistance = properties.lensFocusDistance;
            this.shouldSaveRaw = properties.shouldSaveRaw;
            this.shouldSaveJpeg = properties.shouldSaveJpeg;
        }

        public CaptureSettings makeCopy() {
            return new CaptureSettings(exposureTime,
                    sensitivity,
                    focusDistance,
                    shouldSaveRaw,
                    shouldSaveJpeg);
        }
    }

    public static class TimedCaptureRequest {
        public long mTime;
        public CaptureSettings mSettings;

        public TimedCaptureRequest(long time, CaptureSettings settings) {
            mTime = time;
            mSettings = settings;
        }
    }

    public static class IntervalProperties {
        public Integer sensorSensitivity;
        public Long sensorExposureTime;
        public Float lensFocusDistance;
        public Long spacing;
        public Boolean shouldSaveRaw;
        public Boolean shouldSaveJpeg;

        public IntervalProperties(Integer sensorSensitivity,
                                  Long sensorExposureTime,
                                  Float lensFocusDistance,
                                  Long spacing,
                                  Boolean shouldSaveRaw,
                                  Boolean shouldSaveJpeg) {
            this.sensorSensitivity = sensorSensitivity;
            this.sensorExposureTime = sensorExposureTime;
            this.lensFocusDistance = lensFocusDistance;
            this.spacing = spacing;
            this.shouldSaveRaw = shouldSaveRaw;
            this.shouldSaveJpeg = shouldSaveJpeg;
        }
    }

    // Properties for interval where exposure is varied in steps
    public static class SteppedIntervalProperties {

    }

    public static class CaptureInterval {

        public CaptureSettings settings;
        // start of interval in milliseconds
        public long startTime;
        // length of capture interval in milliseconds
        public long duration;
        // frequency with which captures are taken

        public long spacing;

        public CaptureInterval(CaptureSettings settings, long spacing, long startTime, long duration) {
            this.settings = settings;
            this.spacing = spacing;
            this.startTime = startTime;
            this.duration = duration;
        }

        public CaptureInterval(IntervalProperties properties, long startTime, long duration) {
            this.settings = new CaptureSettings(properties);
            spacing = properties.spacing;
            this.startTime = startTime;
            this.duration = duration;
        }

        public Queue<TimedCaptureRequest> getTimedRequests() {

            Queue<TimedCaptureRequest> requests = new LinkedList<>();
            long time = startTime;
            if (spacing > 0) {
                while (time < startTime + duration) {
                    requests.add(new TimedCaptureRequest(time, settings));
                    time = time + spacing;
                }
            }
            return requests;
        }
    }

    //public List<CaptureInterval> mCaptureIntervals;
    private Queue<TimedCaptureRequest> requestQueue;

    public CaptureSequence(List<CaptureInterval> captureIntervals) {
//        mCaptureIntervals = captureIntervals;
        requestQueue = new LinkedList<>();
        for (CaptureInterval interval : captureIntervals) {
            requestQueue.addAll(interval.getTimedRequests());
        }

    }

    public CaptureSequence(CaptureInterval interval) {
        requestQueue = new LinkedList<>();
        requestQueue.addAll(interval.getTimedRequests());
//        mCaptureIntervals = new ArrayList<>();
//        mCaptureIntervals.add(interval);
    }

    public CaptureSequence(CaptureSettings[] settings,long startTime) {
        requestQueue = new LinkedList<>();
        long time = startTime;

        for(int i = 0; i < settings.length; i++) {
            requestQueue.add(new TimedCaptureRequest(time,settings[i]));
            // the term of 500000 mills gives a bit of spacing between the captures.
            time = time + settings[i].exposureTime/1000000 + 200;
            Log.i("CaptureSequence",String.valueOf(time));
        }
        Log.i("CaptureSequence","finished loop!");
    }

    public Queue<TimedCaptureRequest> getRequestQueue() {
//        Queue<TimedCaptureRequest> queue = new LinkedList<>();
//        for (CaptureInterval interval : mCaptureIntervals) {
//            queue.addAll(interval.getTimedRequests());
//        }
        return requestQueue;
    }

    public int numberCapturesRemaining() {
        int result = 0;
        Calendar c = Calendar.getInstance();
        Long currentTime = c.getTimeInMillis();

        Queue<TimedCaptureRequest> q = getRequestQueue();

       for (TimedCaptureRequest request : q) {
           if (request.mTime > currentTime) {
               result = result + 1;
           }
       }
        return result;

    }


    // Helper method used for debugging
    private static String timeString(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());

    }


}
