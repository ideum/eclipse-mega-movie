package ideum.com.megamovie.Java;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

/**
 * Created by MT_User on 1/26/2017.
 */

public class CaptureSequence {
    public static class CaptureSettings {
        private long mExposureTime;
        private int mSensitivity;
        private float mFocusDistance;
        public static final String TAG = "CaptureSequence";

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

    public static class TimedCaptureRequest {
        public long mTime;
        public CaptureSettings mSettings;

        public TimedCaptureRequest(long time,CaptureSettings settings) {
            mTime = time;
            mSettings = settings;
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

        public Queue<TimedCaptureRequest> getTimedRequests() {

            Queue<TimedCaptureRequest> requests = new LinkedList<>();
            long time = mStartTime;
            if (spacing > 0) {
                while (time < mStartTime + mDuration) {
                    requests.add(new TimedCaptureRequest(time,mSettings));
                    time = time + spacing;
                }
            }
            return requests;
        }
    }

    public Queue<CaptureInterval> mCaptureIntervals;

    public CaptureSequence(Queue<CaptureInterval> captureIntervals) {
        mCaptureIntervals = captureIntervals;
    }

    public Queue<CaptureInterval> getIntervals() {
        return mCaptureIntervals;
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        for(CaptureInterval interval : mCaptureIntervals) {
            totalDuration += interval.getDuration();
        }
        return totalDuration;
    }

    public Queue<TimedCaptureRequest> getRequestQueue() {
        Queue<TimedCaptureRequest> queue = new LinkedList<>();
        for (CaptureInterval interval : mCaptureIntervals) {
            queue.addAll(interval.getTimedRequests());
        }
//        for(TimedCaptureRequest request : queue) {
//            Log.e("CaptureSequence",String.valueOf(request.mTime));
//        }

        return queue;
    }

    private String timeString(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());

    }



}
