package ideum.com.megamovie.Java;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public CaptureSequence buildSequence() {
        CaptureSequence.CaptureSettings settings = mConfig.getSettings();
        int[] spacings = mConfig.getCaptureSpacing();


        long contact1 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT1, mLocation);
        long contact2 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT2, mLocation);
        long contact2_end = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT2_END, mLocation);
        long contact3 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT3, mLocation);
        long contact3_end = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT3_END, mLocation);
        long contact4 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT4, mLocation);

        Queue<CaptureSequence.CaptureInterval> intervals = new LinkedList<>();

        long duration1 = contact2 - contact1;
        intervals.add(new CaptureSequence.CaptureInterval(settings, contact1,duration1,spacings[0],"first partial"));

        long duration2 = contact2_end - contact2;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact2,duration2,spacings[1],"second contact"));

        long duration3 = contact3 - contact2_end;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact2_end,duration3,spacings[2],"annular"));

        long duration4 = contact3_end - contact3;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact3,duration4,spacings[3],"third contact"));

        long duration5 = contact4 - contact3_end;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact3_end,duration5,spacings[4],"second partial"));

        return new CaptureSequence(intervals);

    }

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
