package ideum.com.eclipsecamera2019.Java.CameraControl;

public class CaptureSequenceBuilderDummy  {


    public static  CaptureSequence makeSequence(long c2Time) {
        Long duration = 5000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg,false);

        Long startTime = c2Time;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings, spacing, startTime, 10000L);


        return new CaptureSequence(interval);
    }

    public static  CaptureSequence makeVideoTestSequence(long c2Time) {
        Long duration = 5000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg,false);

        Long startTime = c2Time;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings, spacing, startTime, 5000L);


        return new CaptureSequence(interval);
    }


}
