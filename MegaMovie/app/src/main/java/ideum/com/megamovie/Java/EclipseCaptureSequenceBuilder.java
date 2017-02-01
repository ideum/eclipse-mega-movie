package ideum.com.megamovie.Java;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class EclipseCaptureSequenceBuilder {
    // These are set as constants for now, to be read from config file eventually
    public static final long EXPOSURE_TIME = 5000000;
    public static final int SENSITIVITY = 100;
    public static final float FOCUS_DISTANCE = 0;
    public static final long BB_DURATION = 300;
    public static final int[] NUMBER_CAPTURES = {2,2,2,2,2};

    private LatLng mLocation;

    public EclipseCaptureSequenceBuilder(LatLng location) {
        mLocation = location;
    }

    public CaptureSequence buildSequence() {
        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(EXPOSURE_TIME,SENSITIVITY,FOCUS_DISTANCE);

        EclipseTimeCalculator eclipseTimeCalculator = new EclipseTimeCalculator();
        long contact1 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Contact.CONTACT1,mLocation);
        long contact2 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Contact.CONTACT2,mLocation);
        long contact3 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Contact.CONTACT3,mLocation);
        long contact4 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Contact.CONTACT4,mLocation);

        List<CaptureSequence.CaptureInterval> intervals = new ArrayList<>();

        long duration1 = contact2 - contact1;
        long frequency1 = NUMBER_CAPTURES[0]/duration1;
        intervals.add(new CaptureSequence.CaptureInterval(settings, contact1,duration1,frequency1));

        long duration2 = BB_DURATION;
        long frequency2 = NUMBER_CAPTURES[1]/duration2;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact2,duration2,frequency2));

        long duration3 = contact3 - contact1 - BB_DURATION;
        long frequency3 = NUMBER_CAPTURES[2]/duration3;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact2+BB_DURATION,duration3,frequency3));

        long duration4 = BB_DURATION;
        long frequency4 = NUMBER_CAPTURES[3]/duration4;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact3,duration4,frequency4));

        long duration5 = contact4 - contact3 - BB_DURATION;
        long frequency5 = NUMBER_CAPTURES[4]/duration5;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact4 + BB_DURATION,duration5,frequency5));

        return new CaptureSequence(intervals);

    }


}
