package ideum.com.megamovie.Java.OrientationController;

/**
 * Created by MT_User on 6/30/2017.
 */

public class FixedClock implements Clock {

    public FixedClock(long time) {
        this.time = time;
    }

    private long time;
    public void setTime(long newTime) {
        this.time = newTime;
    }
    @Override
    public long getTimeInMillisSinceEpoch() {
        return time;
    }
}
