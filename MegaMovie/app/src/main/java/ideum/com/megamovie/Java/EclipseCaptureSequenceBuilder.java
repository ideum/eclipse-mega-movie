package ideum.com.megamovie.Java;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class EclipseCaptureSequenceBuilder {

    private LatLng mLocation;
    private ConfigParser mConfig;

    public EclipseCaptureSequenceBuilder(LatLng location,ConfigParser config) {
        mLocation = location;
        mConfig = config;
    }

    public CaptureSequence buildSequence() {
        CaptureSequence.CaptureSettings settings = mConfig.getSettings();
        int[] spacings = mConfig.getCaptureSpacing();

        EclipseTimeCalculator eclipseTimeCalculator = new EclipseTimeCalculator();
        long contact1 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT1, mLocation);
        long contact2 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT2, mLocation);
        long contact2_end = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT2_END, mLocation);
        long contact3 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT3, mLocation);
        long contact3_end = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT3_END, mLocation);
        long contact4 = eclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT4, mLocation);

        List<CaptureSequence.CaptureInterval> intervals = new ArrayList<>();

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


}
