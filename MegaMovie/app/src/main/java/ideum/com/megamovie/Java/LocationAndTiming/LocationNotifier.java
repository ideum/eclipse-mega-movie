package ideum.com.megamovie.Java.LocationAndTiming;

import com.google.android.gms.location.LocationListener;

/**
 * Created by MT_User on 6/2/2017.
 */

public interface LocationNotifier {
    public void addLocationListener(LocationListener listener);
}
