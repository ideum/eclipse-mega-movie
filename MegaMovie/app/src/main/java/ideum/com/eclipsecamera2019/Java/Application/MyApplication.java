package ideum.com.eclipsecamera2019.Java.Application;

import android.app.Application;

import java.io.IOException;

import ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes;
import ideum.com.eclipsecamera2019.Java.NewUI.EclipseInfoFragment;

/**
 * Created by MT_User on 5/10/2017.
 */

public class MyApplication extends Application {

    public EclipseTimes eclipseTimes;
    public Class currentFragment = EclipseInfoFragment.class;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            eclipseTimes = new EclipseTimes(this);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


}
