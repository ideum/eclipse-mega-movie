package ideum.com.megamovie.Java;


import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CaptureSequenceTimer {
    private CaptureSequence mCaptureSequence;
    private CameraFragment mCameraFragment;

    public CaptureSequenceTimer(CameraFragment cameraFragment, CaptureSequence captureSequence) {
        mCameraFragment = cameraFragment;
        mCaptureSequence = captureSequence;
    }

    private long getTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public void startTimer() {
        final Map<Long,CaptureSequence.CaptureSettings> timedRequests = new HashMap<Long,CaptureSequence.CaptureSettings>();
        for (CaptureSequence.CaptureInterval interval : mCaptureSequence.getIntervals()) {
            timedRequests.putAll(interval.getTimedRequests());
        }
        new CountDownTimer(mCaptureSequence.getTotalDuration(), 10) {
            public void onTick(long millisUntilFinished) {
                Iterator it = timedRequests.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Long time = (Long) pair.getKey();
                    if (time <= getTime()) {
                        CaptureSequence.CaptureSettings s = (CaptureSequence.CaptureSettings) pair.getValue();
                        mCameraFragment.takePhotoWithSettings(s);
                        Log.e("CaptureSequenceTimer","photo taken!");
                        it.remove();
                    }
                }
            }
            public void onFinish() {
            }
        }.start();

    }

}
