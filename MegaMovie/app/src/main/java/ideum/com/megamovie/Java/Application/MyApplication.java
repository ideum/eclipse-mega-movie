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

    public EclipseTimes eclipseTimes;
    public Class currentFragment = EclipseInfoFragment.class;

    @Override
    public void onCreate() {
        super.onCreate();
        EclipseTimingMap etm = null;

        try {
            eclipseTimes = new EclipseTimes(this);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


}
