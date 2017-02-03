package ideum.com.megamovie.Java;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;


public class EclipseTimeCalculator {
    public enum Event {
        CONTACT1,
        CONTACT2,CONTACT2_END,
        CONTACT3,CONTACT3_END,
        CONTACT4;
    }

    // This method is just a stand-in for now
    public long eclipseTime(Event event, LatLng location) {
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH,7);
//        calendar.set(Calendar.DAY_OF_MONTH,21);
//        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,28);
        calendar.set(Calendar.SECOND,0);

//        calendar.set(Calendar.SECOND,calendar.get(Calendar.SECOND)+2);
        long startTime = calendar.getTimeInMillis();
        long contactTime = 0;

        switch(event) {
            case CONTACT1:
                contactTime = startTime;
                break;
            case CONTACT2:
                contactTime = startTime + 5000;
                break;
            case CONTACT2_END:
                contactTime = startTime + 5000;
                break;
            case CONTACT3:
                contactTime = startTime + 5000;
                break;
            case CONTACT3_END:
                contactTime = startTime + 5000;
                break;
            case CONTACT4:
                contactTime = startTime + 5000;
                break;
        }
        return contactTime;
    }
}
