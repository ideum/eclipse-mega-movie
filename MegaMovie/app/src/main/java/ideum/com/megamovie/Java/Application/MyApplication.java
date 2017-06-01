package ideum.com.megamovie.Java.Application;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import ideum.com.megamovie.Java.PatagoniaTest.EclipseTimeCalculator;
import ideum.com.megamovie.Java.Utility.EclipseTimingMap;
import ideum.com.megamovie.R;

/**
 * Created by MT_User on 5/10/2017.
 */

public class MyApplication extends Application {

    private EclipseTimeCalculator mEclipseTimeCalculator;

    public EclipseTimeCalculator getEclipseTimeCalculator() {
        return mEclipseTimeCalculator;
    }

//    private EclipseTimingMap mEclipseTimingMap;
//    public EclipseTimingMap getEclipseTimingMap() {
//        return mEclipseTimingMap;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mEclipseTimeCalculator = new EclipseTimeCalculator(this);
    }


}
