package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import java.util.Calendar;

import ideum.com.eclipsecamera2019.Java.OrientationController.Clock;

public class GPSCorrectedClock implements Clock {
    public GPSCorrectedClock(long gpsOffset) {
        this.gpsOffset = gpsOffset;
    }

    private long gpsOffset;

    @Override
    public long getTimeInMillisSinceEpoch() {
        return Calendar.getInstance().getTimeInMillis() + gpsOffset;
    }
}
