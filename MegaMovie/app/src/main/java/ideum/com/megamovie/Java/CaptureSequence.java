package ideum.com.megamovie.Java;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MT_User on 1/26/2017.
 */

public class CaptureSequence {
    public static class CaptureSettings {
        private long mExposureTime;
        private int mSensitivity;
        private float mFocusDistance;

        public CaptureSettings(long exposureTime,int sensitivity,float focusDistance) {
            mExposureTime = exposureTime;
            mSensitivity = sensitivity;
            mFocusDistance = focusDistance;
        }

        public long getExposureTime() {
            return mExposureTime;
        }
        public int getSensitivity() {
            return mSensitivity;
        }

        public float getFocusDistance() {
            return mFocusDistance;
        }
    }
    public static class CaptureInterval {
        private CaptureSettings mSettings;
        // start of interval in milliseconds
        private long mStartTime;
        // length of capture interval in milliseconds
        private long mDuration;
        // frequency with which captures are taken
        private long spacing;
        private String name;

        public CaptureInterval(CaptureSettings settings,long startTime,long duration,long spacing,String name) {
            mSettings = settings;
            mStartTime = startTime;
            mDuration = duration;
            this.spacing = spacing;
            this.name = name;
        }
        public CaptureSettings getSettings() {
            return mSettings;
        }
        public long getStartTime() {
            return mStartTime;
        }
        public long getDuration() {
            return mDuration;
        }
        public double getFrequency() {
            return spacing;
        }
        public String getName() { return name; }

        public Map<Long,CaptureSettings> getTimedRequests() {

            HashMap<Long,CaptureSettings> map = new HashMap<>();
            long time = mStartTime;
            if (spacing > 0) {
                while (time < mStartTime + mDuration) {
                    map.put(time, mSettings);
                    time = time + spacing;
                }
            }
            return map;
        }
    }

    public List<CaptureInterval> mCaptureIntervals;

    public CaptureSequence(List<CaptureInterval> captureIntervals) {
        mCaptureIntervals = captureIntervals;
    }

    public List<CaptureInterval> getIntervals() {
        return mCaptureIntervals;
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        for(CaptureInterval interval : mCaptureIntervals) {
            totalDuration += interval.getDuration();
        }
        return totalDuration;
    }

    public Map<Long,CaptureSettings> getTimedRequests() {
        Map<Long,CaptureSettings> timedRequests = new HashMap<>();
        for (CaptureInterval interval : mCaptureIntervals) {
            timedRequests.putAll(interval.getTimedRequests());
        }
        return timedRequests;
    }



}
