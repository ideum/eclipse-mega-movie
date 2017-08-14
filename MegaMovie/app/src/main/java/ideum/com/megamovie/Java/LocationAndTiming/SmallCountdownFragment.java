package ideum.com.megamovie.Java.LocationAndTiming;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import ideum.com.megamovie.R;


public class SmallCountdownFragment extends Fragment
implements MyTimer.MyTimerListener{

    private TextView countdownTextView;
    private Long targetTimeMills;
    private String countdownString;

    public void setTargetTimeMills(Long mills) {
        targetTimeMills = mills;
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

    @Override
    public void onTick() {
        String s = getNewCountdownString();
        if (s == null || s == countdownString) {
            return;
        }
        countdownTextView.setText(s);
        countdownString = s;
    }

    private String getNewCountdownString() {
        if (targetTimeMills == null) {
            return null;
        }
        Long millsRemaining = targetTimeMills - Calendar.getInstance().getTimeInMillis();
        String days = DateUtil.countdownDaysString(millsRemaining);
        String hours = DateUtil.countdownHoursString(millsRemaining);
        String minutes = DateUtil.countdownMinutesString(millsRemaining);
        String seconds = DateUtil.countdownSecondsString(millsRemaining);
        return days + ":" + hours + ":" + minutes + ":" + seconds;
    }
}
