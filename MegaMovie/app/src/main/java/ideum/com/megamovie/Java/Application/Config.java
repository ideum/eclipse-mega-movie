package ideum.com.megamovie.Java.Application;

public class Config {
    /* TESTING OPTIONS */

    // Used in GPSFragment
    public static final boolean SHOULD_USE_DUMMY_LOCATION = true;
    public static final double DUMMY_LATITUDE = -35;// 44.5646;
    public static final double DUMMY_LONGITUDE = -58;// -123.2620;

    //Used in EclipseTimeProvider
    public static final Boolean USE_DUMMY_TIME_C2 = true;
    public static long  DUMMY_C2_LEAD_TIME = 20000;
    // Used in EclipseDayCaptureActivity
    public static final Boolean ECLIPSE_DAY_SHOULD_USE_DUMMY_SEQUENCE = true;

    /* OTHER PARAMETERS */

    // Used in EclipseDayCaptureActivity
    public static final long GPS_UPDATE_CUTOFF_TIME = 10000L;
    public static final long MIN_TOTALITY_DURATION = 30 * 1000L;
    public static final long AUDIO_ALERT_TIME = 18000;

    // Used in CaptureSequenceBuilder

    public static final Long BEADS_EXPOSURE_TIME = 10000000L;
    public static final Long TOTALITY_EXPOSURE_TIME = 1000000L;

    public static final Boolean beadsShouldCaptureRaw = false;
    public static final Boolean beadsShouldCaptureJpeg = true;
    public static final Boolean totalityShouldCaptureRaw = true;
    public static final Boolean totalityShouldCaptureJpeg = false;
    public static final Double[] BEADS_FRACTIONS = {1.0/16.0,1.0/4.0,1.0,4.0,16.0};
    public static final Double[] TOTALITY_FRACTIONS = {1.0,3.0,10.0,30.0,100.0,300.0};
    public static final Long BEADS_LEAD_TIME = 1000L;
    public static final Long BEADS_DURATION = 10000L;
    public static final Long BEADS_SPACING = 200L;
    public static final Long MARGIN = 1500L;
    public static final Long minRAWMargin = 1200l;

    //estimated max size of single jpeg in megabytes
    public static final float JPEG_SIZE = 0.3f;
    //estimated max size of single dng in megabytes
    public static final float RAW_SIZE = 25.0f;
    // max amount of data we're allowed to save in megabytes
    public static final float DATA_BUDGET = 1000f;


    // Used in EclipseTimingPatch
    public final static int ECLIPSE_BASETIME_YEAR = 2019;
    public final static int ECLIPSE_BASETIME_MONTH = 8;
    public final static int ECLIPSE_BASETIME_DAY = 21;
    public final static int ECLIPSE_BASETIME_HOUR = 0;
    public final static int ECLIPSE_BASETIME_MINUTE = 0;



}
