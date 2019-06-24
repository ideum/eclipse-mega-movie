package ideum.com.eclipsecamera2019.Java.CameraControl;

import ideum.com.eclipsecamera2019.Java.Application.Config;

public class CaptureSequenceBuilder {

    public static CaptureSequence makeSequence(long c2Time,long c3Time,float magnification) {
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
        long c2Duration = c2EndTime - c2StartTime;
        long totalityDuration = totalityEndTime - totalityStartTime;
        long c3Duration = c3EndTime - c3StartTime;


        return new CaptureSequence(intervals);
    }

    public static CaptureSequence makeVideoAndImageSequence(long c2Time, long c3Time, float magnification){
        c3Time = Math.max(c3Time,c2Time + Config.MIN_TOTALITY_DURATION);
        int sensitivity = 60;
        float focusDistance = 0f;

        //long c2BaseExposureTime = (long)( Config.BEADS_EXPOSURE_TIME/(magnification * magnification));
        long c2BaseExposureTime = Config.VIDEO_EXPOSURE_TIME;

        long c2StartTime = c2Time - Config.VIDEO_LEAD_TIME;
        long c2EndTime = c2StartTime + Config.VIDEO_DURATION;

        CaptureSequence.CaptureSettings c2BaseSettings = new CaptureSequence.CaptureSettings(c2BaseExposureTime,sensitivity,focusDistance,false,false);
        c2BaseSettings.isVideo = true;
        long c2Duration = Config.VIDEO_DURATION;
        c2BaseSettings.videoLengthMillis = c2Duration;
        CaptureSequence.CaptureInterval c2Interval = new CaptureSequence.CaptureInterval(c2BaseSettings, c2Duration, c2StartTime, c2Duration);

        //long c3BaseExposureTime = (long)( Config.BEADS_EXPOSURE_TIME/(magnification * magnification));
        long c3BaseExposureTime = Config.VIDEO_EXPOSURE_TIME;
        long c3StartTime = c3Time - Config.VIDEO_LEAD_TIME;

        long totalityBaseExposureTime =  Config.TOTALITY_EXPOSURE_TIME;

        long totalityStartTime = c2EndTime + Config.MARGIN;
        long totalityEndTime = c3StartTime - Config.MARGIN;

        long totalitySpacing = getTotalitySpacing(totalityEndTime - totalityStartTime);

        CaptureSequence.CaptureSettings totalityBaseSettings = new CaptureSequence.CaptureSettings(totalityBaseExposureTime,sensitivity,focusDistance,Config.totalityShouldCaptureRaw,Config.totalityShouldCaptureJpeg);
        CaptureSequence.SteppedInterval totalityInterval = new CaptureSequence.SteppedInterval(totalityBaseSettings, Config.TOTALITY_FRACTIONS,totalityStartTime,totalityEndTime,totalitySpacing);

        CaptureSequence.CaptureSettings c3BaseSettings = new CaptureSequence.CaptureSettings(c3BaseExposureTime,sensitivity,focusDistance,false,false);
        c3BaseSettings.isVideo = true;
        long c3Duration = Config.VIDEO_DURATION;
        c3BaseSettings.videoLengthMillis = c3Duration;
        CaptureSequence.CaptureInterval c3Interval = new CaptureSequence.CaptureInterval(c3BaseSettings, c3Duration, c3StartTime, c3Duration);

        CaptureSequence sequence = new CaptureSequence();
        sequence.AddTimedCaptureRequests(c2Interval.getTimedRequests());
        sequence.AddTimedCaptureRequests(totalityInterval.getRequests());
        sequence.AddTimedCaptureRequests(c3Interval.getTimedRequests());

        return sequence;
    }

    public static CaptureSequence makeSimpleVideoSequence(long c2Time,long c3Time){
        c3Time = Math.max(c3Time,c2Time + Config.MIN_TOTALITY_DURATION);
        float focusDistance = 0f;
        int sensitivity = 60;

        long c2StartTime = c2Time - Config.VIDEO_LEAD_TIME;

        long c3StartTime = c3Time - Config.VIDEO_LEAD_TIME;
        long c3EndTime = c3StartTime + Config.VIDEO_LEAD_TIME;
        long duration = c3EndTime - c2StartTime;

        CaptureSequence.CaptureSettings videoBaseSettings = new CaptureSequence.CaptureSettings(Config.BEADS_EXPOSURE_TIME, sensitivity, focusDistance, false, false);
        videoBaseSettings.isVideo = true;
        videoBaseSettings.videoLengthMillis = duration;
        CaptureSequence.CaptureInterval videoInterval = new CaptureSequence.CaptureInterval(videoBaseSettings, duration, c2StartTime, duration);

        return new CaptureSequence(videoInterval);
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
