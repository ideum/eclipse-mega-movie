package ideum.com.megamovie.Java.Application;

import android.app.Application;
import android.support.v4.app.Fragment;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeCalculator;
import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.NewUI.EclipseDay.EclipseDayIntroFragment;
import ideum.com.megamovie.Java.NewUI.EclipseInfoFragment;

/**
 * Created by MT_User on 5/10/2017.
 */

public class MyApplication extends Application {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    public EclipseTimeCalculator getEclipseTimeCalculator() {
        return mEclipseTimeCalculator;
    }
    public Class currentFragment = EclipseInfoFragment.class;



    @Override
    public void onCreate() {
        super.onCreate();
        mEclipseTimeCalculator = new EclipseTimeCalculator(this);
    }

}
