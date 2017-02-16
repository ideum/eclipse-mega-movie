package ideum.com.megamovie.Java;


import android.location.Location;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class EclipseCaptureSequenceBuilder {

    public final static String TAG = "SequenceBuilder";
    private LocationProvider mLocationProvider;
    private ConfigParser mConfig;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private static final long INTERVAL_LENGTH = 5 * 1000;//30*1000;
    /**
     * How long before each contact to start recording images
     */
    private static final long INTERVAL_STARTING_OFFSET = 5 * 1000;

    public EclipseCaptureSequenceBuilder(LocationProvider provider, ConfigParser config, EclipseTimeCalculator eclipseTimeCalculator) {
        mLocationProvider = provider;
        mConfig = config;
        mEclipseTimeCalculator = eclipseTimeCalculator;
    }

    public CaptureSequence buildSequence() throws IOException, XmlPullParserException {
        List<CaptureSequence.IntervalProperties> iProperties = mConfig.getIntervalProperties();

        Queue<CaptureSequence.CaptureInterval> intervals = new LinkedList<>();

        Location currentLocation = mLocationProvider.getLocation();

        Long contact2 = mEclipseTimeCalculator.getEclipseTime(currentLocation, EclipseTimeCalculator.Event.CONTACT2);
        if (contact2 != null) {
            long startingTime2 = contact2 - INTERVAL_STARTING_OFFSET;
            intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(0), startingTime2, INTERVAL_LENGTH));
        }
        Long contact3 = mEclipseTimeCalculator.getEclipseTime(currentLocation, EclipseTimeCalculator.Event.CONTACT3);

        if (contact3 != null) {
            long startingTime3 = contact3 - INTERVAL_STARTING_OFFSET;
            intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(1), startingTime3, INTERVAL_LENGTH));
        }

        return new CaptureSequence(intervals);
    }

    /**
     * Helper functions used for debugging
     */

    private String dateFromMills(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());
    }

    private String timeString(Long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);

        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);

        return formatter.format(calendar.getTime());

    }

}
