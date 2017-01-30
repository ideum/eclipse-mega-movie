package ideum.com.megamovie.Java;

import android.icu.util.Calendar;
import android.location.Location;


public class EclipseTimeCalculator {
    private Calendar eclipse;

    public EclipseTimeCalculator() {
         eclipse = Calendar.getInstance();
        eclipse.set(Calendar.MONTH,7);
        eclipse.set(Calendar.DAY_OF_MONTH,21);
        eclipse.set(Calendar.HOUR,0);
        eclipse.set(Calendar.MINUTE,0);
//        eclipse.set(Calendar.SECOND,eclipse.get(Calendar.SECOND) + 10);

    }

    public long calculateEclipseTimeInMills(double longitude,double latitude) {
        return eclipse.getTimeInMillis();
    }
}
