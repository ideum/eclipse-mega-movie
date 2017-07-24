/**
 * Controls the automatic capturing of images using a Capture Sequence
 * and Camera Fragment
 */

package ideum.com.megamovie.Java.CameraControl;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import ideum.com.megamovie.Java.LocationAndTiming.LocationProvider;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;

public class CaptureSequenceSession implements MyTimer.MyTimerListener {
    public interface CaptureSessionCompletionListerner {
        public void onSessionCompleted(CaptureSequenceSession session);
    }

    public static final String TAG = "CaptureSequenceSession";
   // private LocationProvider mLocationProvider;
    private Queue<CaptureSequence.TimedCaptureRequest> requestQueue;
    private CaptureSequence.TimedCaptureRequest nextRequest;
    private CameraController mCameraController;
    private List<CaptureSessionCompletionListerner> listeners = new ArrayList<>();

    public void addListener(CaptureSessionCompletionListerner listener) {
        listeners.add(listener);
    }


    public interface CameraController {
        void takePhotoWithSettings(CaptureSequence.CaptureSettings settings);
    }

    public CaptureSequenceSession(CaptureSequence captureSequence, LocationProvider locationProvider, CameraController controller) {
       // mLocationProvider = locationProvider;
        requestQueue = captureSequence.getRequestQueue();
        mCameraController = controller;
    }

    public CaptureSequenceSession(CaptureSequence captureSequence, CameraController controller) {
        requestQueue = captureSequence.getRequestQueue();
        mCameraController = controller;
    }


    private Long getTime() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();

//        Location currentLocation = mLocationProvider.getLocation();
//        if (currentLocation == null) {
//            return null;
//        }
//        return currentLocation.getTime();
    }

    @Override
    public void onTick() {

        Long currentTime = getTime();
        if (currentTime == null) {
            return;
        }
        if (nextRequest == null) {
            seekToNextRequest();
        }
        if (nextRequest != null) {
            Long requestTime = nextRequest.mTime;


            if (currentTime >= requestTime) {
                mCameraController.takePhotoWithSettings(nextRequest.mSettings);
                nextRequest = null;
            }
        }
    }

    /**
     * Sets nextRequest equal to first request whose timestamp is in the future
     */
    private void seekToNextRequest() {
        nextRequest = requestQueue.poll();
        if (nextRequest == null) {
            onCompleted();

            return;
        }
        Long currentTime = getTime();
        if (currentTime == null) {
            return;
        }
        Long requestTime = nextRequest.mTime;
        while (currentTime >= requestTime) {
            nextRequest = requestQueue.poll();
            if (nextRequest == null) {
                return;
            }
            requestTime = nextRequest.mTime;
        }
    }

    private void onCompleted() {
        for (CaptureSessionCompletionListerner listener : listeners) {
            listener.onSessionCompleted(this);
        }
    }


    /**
     * Helper method used for debugging
     */
    private String timeString(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());

    }
}
