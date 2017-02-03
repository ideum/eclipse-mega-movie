package ideum.com.megamovie.Java;


import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CaptureSequenceTimer {
    public static final String TAG = "CaptureSequenceTimer";
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
        Log.e(TAG,"Starting capture sequence");
        final Map<Long,CaptureSequence.CaptureSettings> timedRequests = mCaptureSequence.getTimedRequests();
        Log.e(TAG, String.valueOf(timedRequests.keySet().size()));
        new CountDownTimer(1000000, 10) {
            public void onTick(long millisUntilFinished) {
                Iterator it = timedRequests.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Long time = (Long) pair.getKey();
                    if (time <= getTime()) {
                        CaptureSequence.CaptureSettings s = (CaptureSequence.CaptureSettings) pair.getValue();
                        mCameraFragment.takePhotoWithSettings(s);
                        it.remove();
                    }
                }
            }
            public void onFinish() {
                Log.e(TAG,"Capture sequence completed!");
            }
        }.start();

    }

}
