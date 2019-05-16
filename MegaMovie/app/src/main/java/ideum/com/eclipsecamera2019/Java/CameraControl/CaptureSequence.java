/**
 * Encodes the data for a sequence of camera captures.
 */

package ideum.com.eclipsecamera2019.Java.CameraControl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * Classes for specifying sequence of camera captures. A sequence consists of a collection of timed capture requests,
 * where each request can have different settings for exposure duration etc. A convenient way to construct a CaptureSequence
 * is from a sequence of time intervals, during each of which the camera settings and frequency of captures are fixed.
 */

public class CaptureSequence {

    private Queue<TimedCaptureRequest> requestQueue;

    public static class CaptureSettings {
            public long exposureTime;
            public int sensitivity;
            public float focusDistance;
            public boolean shouldSaveRaw;
            public boolean shouldSaveJpeg;
            public boolean isVideo = false;
            public long videoLengthMillis = 0;

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

//    // Properties for interval where exposure is varied in steps
//    public static class SteppedIntervalProperties {
//
//    }

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

    public static class SteppedInterval {
        CaptureSettings baseSettings;
        Long startTime;
        Long endTime;
        Long spacing;
        Double[] exposureFractions;

        public SteppedInterval(CaptureSettings baseSettings,Double[] exposureFractions, Long startTime,Long endTime,long spacing){
            this.baseSettings = baseSettings;
            this.exposureFractions = exposureFractions;
            this.startTime = startTime;
            this.endTime = endTime;
            this.spacing = spacing;
        }

        public Queue<TimedCaptureRequest> getRequests() {
            LinkedList<TimedCaptureRequest> q = new LinkedList<>();
            Long captureTime = startTime;
            int captureNumber = 0;
            while (captureTime < endTime) {
                CaptureSettings settings = baseSettings.makeCopy();
                settings.exposureTime = (long)(baseSettings.exposureTime * exposureFractions[captureNumber % exposureFractions.length]);
                q.add(new TimedCaptureRequest(captureTime,settings));
                captureTime += spacing;
                captureNumber++;
            }
            return q;
        }
    }

    public CaptureSequence(Queue<TimedCaptureRequest> requestQueue) {
        this.requestQueue = requestQueue;
    }


    public CaptureSequence(SteppedInterval[] intervals) {
        requestQueue = new LinkedList<>();
        for(int i = 0;i< intervals.length;i++) {
            requestQueue.addAll(intervals[i].getRequests());
        }
    }


    public CaptureSequence(CaptureInterval interval) {
        requestQueue = new LinkedList<>();
        requestQueue.addAll(interval.getTimedRequests());

    }


    public Queue<TimedCaptureRequest> getRequestQueue() {
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
