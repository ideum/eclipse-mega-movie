package ideum.com.eclipsecamera2019.Java.CameraControl;

import java.util.Queue;

public class CaptureSequenceBuilderDummy {


    public static CaptureSequence makeSequence(long c2Time) {
        Long duration = 5000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg);

        Long startTime = c2Time;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings, spacing, startTime, 10000L);


        return new CaptureSequence(interval);
    }

    public static CaptureSequence makeVideoTestSequence(long c2Time) {
        Long duration = 50000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg);

        Long startTime = c2Time;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings, spacing, startTime, 2000L);
        Queue<CaptureSequence.TimedCaptureRequest> q = interval.getTimedRequests();

        CaptureSequence.CaptureSettings vidSettings = new CaptureSequence.CaptureSettings(duration/10, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg);
        vidSettings.isVideo = true;
        vidSettings.videoLengthMillis = 2000L;
        q.add(new CaptureSequence.TimedCaptureRequest(startTime + 3000L, vidSettings));
        return new CaptureSequence(q);
    }


}
