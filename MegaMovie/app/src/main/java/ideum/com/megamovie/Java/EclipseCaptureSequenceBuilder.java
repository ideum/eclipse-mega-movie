package ideum.com.megamovie.Java;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class EclipseCaptureSequenceBuilder {
    public static final long EXPOSURE_TIME = 5000000;
    public static final int SENSITIVITY = 100;
    public static final float FOCUS_DISTANCE = 0;

    private double mLatitude;
    private double mLongitude;
    private int[] mNumberOfCaptures;

    public EclipseCaptureSequenceBuilder(LatLng location, int[] numberOfCaptures) {
        mLatitude = location.latitude;
        mLongitude = location.longitude;
        mNumberOfCaptures = numberOfCaptures;
    }

    public CaptureSequence buildSequence() {
        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(EXPOSURE_TIME,SENSITIVITY,FOCUS_DISTANCE);

        EclipseTimeCalculator eclipseTimeCalculator = new EclipseTimeCalculator();
        long contact1 = eclipseTimeCalculator.eclipseFirstContact(mLongitude,mLatitude);
        long contact2 = eclipseTimeCalculator.eclipseSecondContact(mLongitude,mLatitude);
        long contact3 = eclipseTimeCalculator.eclipseThirdContact(mLongitude,mLatitude);
        long contact4 = eclipseTimeCalculator.eclipseFourthContact(mLongitude,mLatitude);

        List<CaptureSequence.CaptureInterval> intervals = new ArrayList<>();

        long duration1 = contact2 - contact1;
        long frequency1 = mNumberOfCaptures[0]/duration1;
        intervals.add(new CaptureSequence.CaptureInterval(settings, contact1,duration1,frequency1));

        long duration2 = contact3 - contact2;
        long frequency2 = mNumberOfCaptures[1]/duration2;
        intervals.add(new CaptureSequence.CaptureInterval(settings,contact2,duration2,frequency2));

        return new CaptureSequence(intervals);

    }


}
