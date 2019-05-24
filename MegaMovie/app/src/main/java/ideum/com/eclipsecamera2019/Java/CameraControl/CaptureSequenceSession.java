/**
 * Controls the automatic capturing of images using a Capture Sequence
 * and Camera Fragment
 */

package ideum.com.eclipsecamera2019.Java.CameraControl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.OrientationController.Clock;

public class CaptureSequenceSession implements MyTimer.MyTimerListener {
    public interface CaptureSessionCompletionListerner {
        void onSessionCompleted(CaptureSequenceSession session);
    }

    private boolean isRecordingVideo = false;
    private long videoRecordingStartTime;
    private long videoDuration;

    private boolean inProgress = false;

    public void start() {
        inProgress = true;
    }

    public void stop() {
        inProgress = false;

    }

    public static final String TAG = "CaptureSequenceSession";
    private Queue<CaptureSequence.TimedCaptureRequest> requestQueue;
    private CaptureSequence.TimedCaptureRequest nextRequest;
    private CaptureSessionListener mCameraController;
    private List<CaptureSessionCompletionListerner> listeners = new ArrayList<>();
    public Clock timeProvider;

    public CaptureSequenceSession(CaptureSequence captureSequence, CaptureSessionListener controller,Clock timeProvider) {
        requestQueue = captureSequence.getRequestQueue();
        mCameraController = controller;
        this.timeProvider = timeProvider;
    }

    public void addListener(CaptureSessionCompletionListerner listener) {
        listeners.add(listener);
    }


    public interface CaptureSessionListener {
        void takePhotoWithSettings(CaptureSequence.CaptureSettings settings);

        void startRecordingVideo(CaptureSequence.CaptureSettings settings);

        void stopRecordingVideo();
    }


    private Long getTime() {
        if(timeProvider != null) {
            return timeProvider.getTimeInMillisSinceEpoch();
        }
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

    @Override
    public void onTick() {
        if (!inProgress) {
            return;
        }
        Long currentTime = getTime();
        if (currentTime == null) {
            return;
        }
        if(isRecordingVideo) {
            if(currentTime >= videoRecordingStartTime + videoDuration) {
                mCameraController.stopRecordingVideo();
                isRecordingVideo = false;
            }
            return;
        }


        if (nextRequest == null) {
            seekToNextRequest();
        }
        if (nextRequest == null) {
            onCompleted();
            return;
        } else {
            Long requestTime = nextRequest.mTime;

            if (currentTime >= requestTime) {
                sendRequest(nextRequest);
                nextRequest = null;
            }
        }
    }

    private void sendRequest(CaptureSequence.TimedCaptureRequest request) {
        if (isRecordingVideo) {
            return;
        }
        if (!request.mSettings.isVideo) {
            mCameraController.takePhotoWithSettings(request.mSettings);
        } else {
            mCameraController.startRecordingVideo(request.mSettings);
            isRecordingVideo = true;
            videoRecordingStartTime = request.mTime;
            videoDuration = request.mSettings.videoLengthMillis;
        }

    }

    /**
     * Sets nextRequest equal to first request whose timestamp is in the future
     */
    private void seekToNextRequest() {
        nextRequest = requestQueue.poll();
        if (nextRequest == null) {
            //onCompleted();
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
        if (!inProgress) {
            return;
        }
        stop();
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
