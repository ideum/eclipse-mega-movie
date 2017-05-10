package ideum.com.megamovie.Java.Application;

import android.app.Application;

import java.io.IOException;

import ideum.com.megamovie.Java.Utility.EclipseTimingMap;

/**
 * Created by MT_User on 5/10/2017.
 */

public class MyApplication extends Application {

    private EclipseTimingMap mEclipseTimingMap;
    public EclipseTimingMap getEclipseTimingMap() {
        return mEclipseTimingMap;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mEclipseTimingMap = new EclipseTimingMap(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
