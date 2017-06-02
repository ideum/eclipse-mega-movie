package ideum.com.megamovie.Java.Application;

import android.app.Application;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeCalculator;

/**
 * Created by MT_User on 5/10/2017.
 */

public class MyApplication extends Application {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    public EclipseTimeCalculator getEclipseTimeCalculator() {
        return mEclipseTimeCalculator;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mEclipseTimeCalculator = new EclipseTimeCalculator(this);
    }


}
