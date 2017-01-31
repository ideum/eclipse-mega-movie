package ideum.com.megamovie.Java;

import android.hardware.camera2.CaptureRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        private long mFrequency;

        public CaptureInterval(CaptureSettings settings,long startTime,long duration,long frequency) {
            mSettings = settings;
            mStartTime = startTime;
            mDuration = duration;
            mFrequency = frequency;
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
        public long getFrequency() {
            return mFrequency;
        }

        public HashMap<Long,CaptureSettings> getTimedRequests() {
            HashMap<Long,CaptureSettings> map = new HashMap<>();
            long time = mStartTime;
            while(time < mStartTime + mDuration) {
                map.put(time,mSettings);
                time = time + mFrequency;
            }
            return map;
        }

    }

    private List<CaptureInterval> mCaptureIntervals;

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



}
