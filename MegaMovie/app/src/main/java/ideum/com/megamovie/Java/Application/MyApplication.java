package ideum.com.megamovie.Java.Application;

import android.app.Application;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeCalculator;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingPatch;
import ideum.com.megamovie.Java.NewUI.EclipseInfoFragment;

/**
 * Created by MT_User on 5/10/2017.
 */

public class MyApplication extends Application {

   // private EclipseTimeCalculator mEclipseTimeCalculator;

   // public EclipseTimeCalculator getEclipseTimeCalculator() {
    //    return mEclipseTimeCalculator;
   // }
    public EclipseTimes eclipseTimes;
    public Class currentFragment = EclipseInfoFragment.class;

    @Override
    public void onCreate() {
        super.onCreate();
       // mEclipseTimeCalculator = new EclipseTimeCalculator(this);
        EclipseTimingMap etm = null;
//        try {
//             etm = new EclipseTimingMap(this,mEclipseTimeCalculator.c1_timing_files.get(0),
//                    mEclipseTimeCalculator.c2_timing_files.get(0),
//                    mEclipseTimeCalculator.cm_timing_files.get(0),
//                    mEclipseTimeCalculator.c3_timing_files.get(0),
//                    mEclipseTimeCalculator.c4_timing_files.get(0));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            eclipseTimes = new EclipseTimes(this);
        } catch(IOException e) {
            e.printStackTrace();
        }

        EclipseTimingPatch etp = eclipseTimes.patches.get(EclipseTimes.Phase.c1).get(0);

        LatLng p1 = new LatLng(31,-78);
        LatLng p2 = new LatLng(31,-77.995);
        LatLng p3 = new LatLng(31.01,-78);
        LatLng p4 = new LatLng(31.015,-78);

    }


}
