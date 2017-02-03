package ideum.com.megamovie.Java;

import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.TimeUnit;

import ideum.com.megamovie.R;



public class CountdownFragment extends Fragment {
    // view for displaying countdown
    private TextView mTextView;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    public boolean isPrecise = false;
    private LocationProvider mLocationProvider;

    public void setLocationProvider(LocationProvider locationProvider) {
        mLocationProvider = locationProvider;
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
    public void setEclipseTimeCalculator(EclipseTimeCalculator etc) {
        mEclipseTimeCalculator = etc;
    }


//    public void setTargetDateMills(long mills) {
//        targetDateMills = mills;
//    }

    public void updateDisplay() {
        if (isPrecise) {
            mTextView.setText(hmsCountdownString());
        } else {
            mTextView.setText(dhmsCountdownString());
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


        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        return mEclipseTimeCalculator.eclipseTime(EclipseTimeCalculator.Event.CONTACT1,latLng) - rightNow;
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
        long days = TimeUnit.MILLISECONDS.toDays(mills);
        mills -= TimeUnit.DAYS.toMillis(days);

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
}