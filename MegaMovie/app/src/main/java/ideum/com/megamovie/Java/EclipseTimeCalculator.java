package ideum.com.megamovie.Java;


import android.location.Location;

import java.util.Calendar;


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

    public long eclipseFirstContact(double longitude, double latitude) {
        return eclipse.getTimeInMillis();
    }
    public long eclipseSecondContact(double longitude, double latitude) {
        return eclipse.getTimeInMillis()+3000;
    }
    public long eclipseThirdContact(double longitude, double latitude) {
        return eclipse.getTimeInMillis()+6000;
    }
    public long eclipseFourthContact(double longitude, double latitude) {
        return eclipse.getTimeInMillis()+9000;
    }
}
