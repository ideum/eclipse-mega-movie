package ideum.com.eclipsecamera2019.Java.LocationAndTiming;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import ideum.com.eclipsecamera2019.R;


public class SmallCountdownFragment extends Fragment
implements MyTimer.MyTimerListener{

    private TextView countdownTextView;
    private Long targetTimeMills;


    private String countdownString;

    public void setTargetTimeMills(Long mills) {
        targetTimeMills = mills;
        millsRemaining = targetTimeMills - Calendar.getInstance().getTimeInMillis();
       // updateUI();
    }

    private long millsRemaining;
    public void setTimeRemainingMillis(long millis) {
        millsRemaining = millis;
        updateUI();
    }


    public SmallCountdownFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_small_countdown, container, false);

        countdownTextView = rootView.findViewById(R.id.countdown_text_view);


        return rootView;
    }

    private void updateUI() {
        String s = getNewCountdownString();
        if (s == null || s == countdownString) {
            return;
        }
        countdownTextView.setText(s);
        countdownString = s;
        if(millsRemaining < 0) {
            countdownTextView.setTextColor(getResources().getColor(R.color.green_text_color));
        } else {
            countdownTextView.setTextColor(getResources().getColor(R.color.intro_text_color_1));
        }
    }

    @Override
    public void onTick() {
       updateUI();
    }

    private String getNewCountdownString() {
        long millis = Math.abs( millsRemaining);
        String days = DateUtil.countdownDaysString(millis);
        String hours = DateUtil.countdownHoursString(millis);
        String minutes = DateUtil.countdownMinutesString(millis);
        String seconds = DateUtil.countdownSecondsString(millis);
        return days + ":" + hours + ":" + minutes + ":" + seconds;
    }


}
