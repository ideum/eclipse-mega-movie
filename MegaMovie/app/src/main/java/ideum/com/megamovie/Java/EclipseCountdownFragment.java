package ideum.com.megamovie.Java;

import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ideum.com.megamovie.R;



public class EclipseCountdownFragment extends Fragment
implements MyTimer.MyTimerListener{
    // view for displaying countdown
    private TextView mTextView;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    public boolean includesDays = false;
    public static final String TAG = "EclipseCountdownFragment";
    private static final String DEFAULT_MESSAGE = "Can't get contact times";
    private Long millisecondsRemaining;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = (TextView) view.findViewById(R.id.timer_text_view);
    }

    @Override
    public void onTick() {
        setMillisecondsRemaining(millisecondsToContact2());
    }

    private void setMillisecondsRemaining(Long mills) {
        millisecondsRemaining = mills;
        updateDisplay();
    }

    public void updateDisplay() {
        String text = DEFAULT_MESSAGE;
        if (millisecondsRemaining != null) {
            if (includesDays) {
                text = millisecondsToDHMS(millisecondsRemaining);
            } else {
                text = millisecondsToHMS(millisecondsRemaining);
            }
        }
        mTextView.setText(text);
    }

    public Long millisecondsToContact2() {
        if (mEclipseTimeCalculator == null) {
            return null;
        }
        return mEclipseTimeCalculator.getTimeToEvent(EclipseTimeCalculator.Event.CONTACT2);

//        if ( mEclipseTimeCalculator == null || mLocationProvider == null) {
//            return null;
//        }
//
//        Location currentLocation = mLocationProvider.getLocation();
//        if (currentLocation == null) {
//            return null;
//        }
//        Long rightNow = currentLocation.getTime();
//
//
//        Long contact2 = mEclipseTimeCalculator.getEclipseTime(EclipseTimeCalculator.Event.CONTACT2);
//        if (contact2 == null) {
//            return null;
//        }
//        return contact2 - rightNow;
    }

    // Creates string representing time in mills in days, hours, minutes and seconds
    private String millisecondsToDHMS(long mills) {
        long days = TimeUnit.MILLISECONDS.toDays(mills);
        mills -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(mills);
        mills = mills - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mills);
        mills = mills - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mills);

        String result = String.format("%02d", days);
        result += ":" + String.format("%02d", hours);
        result += ":" + String.format("%02d", minutes);
        result += ":" + String.format("%02d", seconds);

        return result;
    }
    // Creates string representing time in mills in hours, minutes, seconds
    private String millisecondsToHMS(long mills) {

        long hours = TimeUnit.MILLISECONDS.toHours(mills);
        mills = mills - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mills);
        mills = mills - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mills);


        String result = "";
        result += String.format("%02d", hours);
        result += ":" + String.format("%02d", minutes);
        result += ":" + String.format("%02d", seconds);

        return result;
    }

    /**
     * Helper function used for debugging
     */
    private String timeOfDayString(Long mills) {
        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);

        return formatter.format(calendar.getTime());
    }

    public void setEclipseTimeCalculator(EclipseTimeCalculator etc) {
        mEclipseTimeCalculator = etc;
    }

}