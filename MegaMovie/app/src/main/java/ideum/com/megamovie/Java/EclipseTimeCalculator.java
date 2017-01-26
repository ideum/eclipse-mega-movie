package ideum.com.megamovie.Java;

import android.icu.util.Calendar;
import android.location.Location;

/**
 * Created by MT_User on 1/25/2017.
 */

public class EclipseTimeCalculator {
    public long calculateEclipseTimeInMills(double longitude,double latitude) {
        Calendar eclipse = Calendar.getInstance();
        eclipse.set(Calendar.MONDAY,7);
        eclipse.set(Calendar.DAY_OF_MONTH,21);
        eclipse.set(Calendar.HOUR,0);
        eclipse.set(Calendar.MINUTE,0);
        eclipse.set(Calendar.SECOND,0);

        return eclipse.getTimeInMillis();
    }
}
