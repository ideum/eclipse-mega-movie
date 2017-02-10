package ideum.com.megamovie.Java;


import com.google.android.gms.maps.model.LatLng;

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
    private LatLng mLocation;
    private ConfigParser mConfig;
    private EclipseTimeCalculator mEclipseTimeCalculator;

    public EclipseCaptureSequenceBuilder(LatLng location,ConfigParser config,EclipseTimeCalculator eclipseTimeCalculator) {
        mLocation = location;
        mConfig = config;
        mEclipseTimeCalculator = eclipseTimeCalculator;
    }

    public CaptureSequence buildSequence() throws IOException, XmlPullParserException {
        List<CaptureSequence.IntervalProperties> iProperties = mConfig.getIntervalProperties();


        long contact1 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT1);
        long contact2 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT2);
        long contact2_end = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT2_END);
        long contact3 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT3);
        long contact3_end = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT3_END);
        long contact4 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT4);

        Queue<CaptureSequence.CaptureInterval> intervals = new LinkedList<>();

        long duration1 = contact2 - contact1;
        intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(0), contact1,duration1));

        long duration2 = contact2_end - contact2;
        intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(1),contact2,duration2));

        long duration3 = contact3 - contact2_end;
        intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(2),contact2_end,duration3));

        long duration4 = contact3_end - contact3;
        intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(3),contact3,duration4));

        long duration5 = contact4 - contact3_end;
        intervals.add(new CaptureSequence.CaptureInterval(iProperties.get(4),contact3_end,duration5));

        return new CaptureSequence(intervals);

    }

    // Helper function used for debugging
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
