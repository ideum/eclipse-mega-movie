/**
 * Encodes the data for a sequence of camera captures.
 */

package ideum.com.megamovie.Java.PatagoniaTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        Integer sensorSensitivity;
        Long sensorExposureTime;
        Float lensFocusDistance;
        Long spacing;
        Boolean shouldSaveRaw;
        Boolean shouldSaveJpeg;

        public IntervalProperties(Integer sensorSensitivity, Long sensorExposureTime, Float lensFocusDistance,
                                  Long spacing, Boolean shouldSaveRaw, Boolean shouldSaveJpeg) {
            this.sensorSensitivity = sensorSensitivity;
            this.sensorExposureTime = sensorExposureTime;
            this.lensFocusDistance = lensFocusDistance;
            this.spacing = spacing;
            this.shouldSaveRaw = shouldSaveRaw;
            this.shouldSaveJpeg = shouldSaveJpeg;
        }
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

    public List<CaptureInterval> mCaptureIntervals;

    public CaptureSequence(List<CaptureInterval> captureIntervals) {
        mCaptureIntervals = captureIntervals;
    }

    public Queue<TimedCaptureRequest> getRequestQueue() {
        Queue<TimedCaptureRequest> queue = new LinkedList<>();
        for (CaptureInterval interval : mCaptureIntervals) {
            queue.addAll(interval.getTimedRequests());
        }
        return queue;
    }


    // Helper method used for debugging
    private String timeString(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());

    }


}
