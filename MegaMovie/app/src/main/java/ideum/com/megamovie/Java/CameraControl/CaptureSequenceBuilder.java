package ideum.com.megamovie.Java.CameraControl;

import java.util.Calendar;

import ideum.com.megamovie.Java.Application.Config;

public class CaptureSequenceBuilder {

    public static CaptureSequence makeSequence(long c2Time,long c3Time,float magnification) {
        if (Config.ECLIPSE_DAY_SHOULD_USE_DUMMY_SEQUENCE) {
            return dummySequence(c2Time);
        }


        c3Time = Math.max(c3Time,c2Time + Config.MIN_TOTALITY_DURATION);
        int sensitivity = 60;
        float focusDistance = 0f;

        long c2BaseExposureTime = (long)( Config.BEADS_EXPOSURE_TIME/(magnification * magnification));
        boolean c2ShouldSaveRaw = Config.beadsShouldCaptureRaw;
        boolean c2ShouldSaveJpeg = Config.beadsShouldCaptureJpeg;

        long c2StartTime = c2Time - Config.BEADS_LEAD_TIME;
        long c2EndTime = c2StartTime + Config.BEADS_DURATION;
        long c2Spacing = Config.BEADS_SPACING;

        CaptureSequence.CaptureSettings c2BaseSettings = new CaptureSequence.CaptureSettings(c2BaseExposureTime,sensitivity,focusDistance,c2ShouldSaveRaw,c2ShouldSaveJpeg);
        CaptureSequence.SteppedInterval c2Interval = new CaptureSequence.SteppedInterval(c2BaseSettings, Config.BEADS_FRACTIONS,c2StartTime,c2EndTime,c2Spacing);

        long c3BaseExposureTime = (long)( Config.BEADS_EXPOSURE_TIME/(magnification * magnification));
        boolean c3ShouldSaveRaw = false;
        boolean c3ShouldSaveJpeg = true;

        long c3StartTime = c3Time - Config.BEADS_LEAD_TIME;
        long c3EndTime = c3StartTime + Config.BEADS_DURATION;
        long c3Spacing = Config.BEADS_SPACING;

        long totalityBaseExposureTime =  Config.TOTALITY_EXPOSURE_TIME;

        long totalityStartTime = c2EndTime + Config.MARGIN;
        long totalityEndTime = c3StartTime - Config.MARGIN;

        long totalitySpacing = getTotalitySpacing(totalityEndTime - totalityStartTime);

        CaptureSequence.CaptureSettings totalityBaseSettings = new CaptureSequence.CaptureSettings(totalityBaseExposureTime,sensitivity,focusDistance,Config.totalityShouldCaptureRaw,Config.totalityShouldCaptureJpeg);
        CaptureSequence.SteppedInterval totalityInterval = new CaptureSequence.SteppedInterval(totalityBaseSettings, Config.TOTALITY_FRACTIONS,totalityStartTime,totalityEndTime,totalitySpacing);

        CaptureSequence.CaptureSettings c3BaseSettings = new CaptureSequence.CaptureSettings(c3BaseExposureTime,sensitivity,focusDistance,c3ShouldSaveRaw,c3ShouldSaveJpeg);
        CaptureSequence.SteppedInterval c3Interval = new CaptureSequence.SteppedInterval(c3BaseSettings, Config.BEADS_FRACTIONS,c3StartTime,c3EndTime,c3Spacing);

        CaptureSequence.SteppedInterval[] intervals = {c2Interval,totalityInterval,c3Interval};


        return new CaptureSequence(intervals);
    }

    private static CaptureSequence dummySequence(long c2Time) {
        Long duration = 5000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg);

        Long startTime = c2Time;// Calendar.getInstance().getTimeInMillis() + 6000;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings, spacing, startTime, 10000L);


        return new CaptureSequence(interval);
    }

    private static long getTotalitySpacing(long duration) {
        int numTotalityCaptures = (int)(totalityDataBudget()/Config.RAW_SIZE);
        long idealSpacing = (long)(duration/(float)numTotalityCaptures);
        return Math.max(idealSpacing,Config.minRAWMargin);
    }

    private static float totalityDataBudget() {
        float beadsDataUsage = 2 * Config.JPEG_SIZE * Config.BEADS_DURATION /(float)Config.BEADS_SPACING;
        return Config.DATA_BUDGET - beadsDataUsage;
    }
}
