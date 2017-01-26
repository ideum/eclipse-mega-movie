package ideum.com.megamovie.Java;

import android.icu.util.Calendar;
import android.location.Location;

/**
 * Created by MT_User on 1/25/2017.
 */

public class EclipseTimeCalculator {
    public long calculateEclipseTimeInMills(Location location) {
        Calendar eclipse = Calendar.getInstance();
        eclipse.set(Calendar.HOUR,6);
        eclipse.set(Calendar.MINUTE,0);
        eclipse.set(Calendar.SECOND,0);

        return eclipse.getTimeInMillis();
    }
}
