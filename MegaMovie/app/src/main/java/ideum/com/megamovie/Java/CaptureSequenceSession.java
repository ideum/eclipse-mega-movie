package ideum.com.megamovie.Java;


import android.location.Location;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class CaptureSequenceSession implements MyTimer.MyTimerListener {
    public static final String TAG = "CaptureSequenceSession";
    private static final long TIMER_DURATION = 1000000;
    private static final long TIMER_INTERVAL = 10;
    //    private CaptureSequence mCaptureSequence;
    private LocationProvider mLocationProvider;
    private Queue<CaptureSequence.TimedCaptureRequest> requestQueue;
    private MyTimer mMyTimer;
    private CaptureSequence.TimedCaptureRequest nextRequest;
    private CameraController mCameraController;

    public interface CameraController {
        void takePhotoWithSettings(CaptureSequence.CaptureSettings settings);
    }

    public CaptureSequenceSession(CaptureSequence captureSequence, LocationProvider locationProvider, CameraController controller) {
//        mCaptureSequence = captureSequence;
        mLocationProvider = locationProvider;
        requestQueue = captureSequence.getRequestQueue();
//        cullRequestQueue();
        mCameraController = controller;
    }


    private Long getTime() {
        Location currentLocation = mLocationProvider.getLocation();
        if (currentLocation == null) {
            return null;
        }
        return currentLocation.getTime();
    }

    @Override
    public void onTick() {
        Long currentTime = getTime();
        if (currentTime == null) {
            return;
        }
        if (nextRequest == null) {
            nextRequest = requestQueue.poll();
        }
        if (nextRequest != null) {
            Long requestTime = nextRequest.mTime;


            if (currentTime >= requestTime) {
                mCameraController.takePhotoWithSettings(nextRequest.mSettings);
                nextRequest = null;
            }
        }
    }

//    private CaptureSequence.TimedCaptureRequest getNextRequest() {
//        nextRequest = requestQueue.poll();
//
//    }


    public void startSession() {
        cancelSession();
        MyTimer timer = new MyTimer(this);
        timer.startTicking();
    }

    public void cancelSession() {
        if (mMyTimer != null) {
            mMyTimer.cancel();
            mMyTimer = null;
        }
    }

    private String timeString(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());

    }
}
