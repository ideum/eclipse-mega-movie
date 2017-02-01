package ideum.com.megamovie.Java;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;


public class EclipseTimeCalculator {
    public enum Contact {
        CONTACT1,CONTACT2,CONTACT3,CONTACT4;
    }

    // This method is just a stand-in for now
    public long eclipseTime(Contact contact, LatLng location) {
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH,7);
//        calendar.set(Calendar.DAY_OF_MONTH,21);
//        calendar.set(Calendar.HOUR,0);
//        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,calendar.get(Calendar.SECOND)+3);
        long contactTime = 0;

        switch(contact) {
            case CONTACT1:
                contactTime = calendar.getTimeInMillis();
                break;
            case CONTACT2:
                contactTime = calendar.getTimeInMillis() + 1000;
                break;
            case CONTACT3:
                contactTime = calendar.getTimeInMillis() + 2000;
                break;
            case CONTACT4:
                contactTime = calendar.getTimeInMillis() + 3000;
                break;
        }
        return contactTime;
    }
}
