package ideum.com.megamovie.Java;


import android.location.Location;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;

public class CaptureSequenceSession {
    public static final String TAG = "CaptureSequenceSession";
    private static final long TIMER_DURATION = 1000000;
    private static final long TIMER_INTERVAL = 1;
    private CaptureSequence mCaptureSequence;
    private CameraFragment mCameraFragment;
    private LocationProvider mLocationProvider;

    public CaptureSequenceSession(CameraFragment cameraFragment, CaptureSequence captureSequence, LocationProvider locationProvider) {
        mCameraFragment = cameraFragment;
        mCaptureSequence = captureSequence;
        mLocationProvider = locationProvider;
    }

    private Long getTime() {
        Location currentLocation = mLocationProvider.getLocation();
        if (currentLocation == null) {
            return null;
        }
        return currentLocation.getTime();
    }

    public void startTimer() {
        final Map<Long,CaptureSequence.CaptureSettings> timedRequests = mCaptureSequence.getTimedRequests();
        Log.e(TAG, "Starting capture timer with number of requests: " + String.valueOf(timedRequests.keySet().size()));
        new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                Iterator it = timedRequests.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Long time = (Long) pair.getKey();
                    Long currentTime = getTime();
                    if (currentTime == null) {
                        continue;
                    }
                    if (time <= currentTime) {
                        Log.e(TAG,"current: " + String.valueOf(currentTime));
                        Log.e(TAG,"planned: " + String.valueOf(time));
                        CaptureSequence.CaptureSettings s = (CaptureSequence.CaptureSettings) pair.getValue();
                        mCameraFragment.takePhotoWithSettings(s);
                        it.remove();
                    }
                }
            }
            public void onFinish() {
                Log.e(TAG,"Timer completed");
            }
        }.start();

    }

}
