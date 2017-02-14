package ideum.com.megamovie.Java;

import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ideum.com.megamovie.R;



public class CountdownFragment extends Fragment
implements MyTimer.MyTimerListener{
    // view for displaying countdown
    private TextView mTextView;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    public boolean includesDays = false;
    private LocationProvider mLocationProvider;
    private MyTimer mTimer;
    public static final String TAG = "CountdownFragment";

    private int count = 0;
    @Override
    public void  onTick() {
        updateDisplay();
    }

    public void setLocationProvider(LocationProvider locationProvider) {
        mLocationProvider = locationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimer = new MyTimer(this);
    }

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
    public void onResume() {
        super.onResume();
        mTimer.startTicking();

    }

    @Override
    public void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    public void setEclipseTimeCalculator(EclipseTimeCalculator etc) {
        mEclipseTimeCalculator = etc;
    }


    public void updateDisplay() {
        if (includesDays) {
            mTextView.setText(dhmsCountdownString());
        } else {
            mTextView.setText(hmsCountdownString());
        }
    }

    public Long millsToTargetDate() {
        if ( mEclipseTimeCalculator == null || mLocationProvider == null) {
            return null;
        }

        Location currentLocation = mLocationProvider.getLocation();
        if (currentLocation == null) {
            return null;
        }
        Long rightNow = currentLocation.getTime();


        Long contact2 = mEclipseTimeCalculator.getEclipseTime(currentLocation, EclipseTimeCalculator.Event.CONTACT2);
        if (contact2 == null) {
            return null;
        }
        return contact2 - rightNow;
    }

    // Creates string representing time in mills in days, hours, minutes and seconds
    private String millsToDHMS(long mills) {
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
    private String millstoHMS(long mills) {
//        long days = TimeUnit.MILLISECONDS.toDays(mills);
//        mills -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(mills);
        mills = mills - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mills);
        mills = mills - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mills);


        String result = "";
        result += String.format("%02d", hours);
        result += ":" + String.format("%02d", minutes);
        result += ":" + String.format("%02d", seconds);
//        result += ":" + String.format("%02d", hundredths);

        return result;
    }

    private String dhmsCountdownString() {
        if (millsToTargetDate() == null) {
            return "Can't access GPS";
        }
        return millsToDHMS(millsToTargetDate());
    }

    private String hmsCountdownString() {
        if (millsToTargetDate() == null) {
            return "Can't access GPS";
        }
        return millstoHMS(millsToTargetDate());
    }

    private String timeOfDayString(Long mills) {
        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);

        return formatter.format(calendar.getTime());
    }
}