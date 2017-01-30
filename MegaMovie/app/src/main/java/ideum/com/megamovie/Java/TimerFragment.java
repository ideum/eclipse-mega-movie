package ideum.com.megamovie.Java;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ideum.com.megamovie.R;



public class TimerFragment extends Fragment {
    // view for displaying countdown
    private TextView mTextView;
    // The date we are counting down to.
    private long targetDateMills;
    public boolean isPrecise = false;

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

    public void setTargetDateMills(long mills) {
        targetDateMills = mills;
    }

    public void updateDisplay() {
        if (isPrecise) {
            mTextView.setText(hmshCountdownString());
        } else {
            mTextView.setText(dhmsCountdownString());
        }
    }

    public long millsToTargetDate() {
        Calendar rightNow = Calendar.getInstance();
        return targetDateMills - rightNow.getTimeInMillis();
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
    // Creates string representing time in mills in hours, minutes, seconds, and hundredths of seconds.
    private String millstoHMSH(long mills) {
        long days = TimeUnit.MILLISECONDS.toDays(mills);
        mills -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(mills);
        mills = mills - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mills);
        mills = mills - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mills);
        mills = mills - TimeUnit.SECONDS.toMillis(seconds);
        long hundredths = (long)(int)mills/10;

        String result = "";
        result += String.format("%02d", hours);
        result += ":" + String.format("%02d", minutes);
        result += ":" + String.format("%02d", seconds);
        result += ":" + String.format("%02d", hundredths);

        return result;
    }

    private String dhmsCountdownString() {

        return millsToDHMS(millsToTargetDate());
    }

    private String hmshCountdownString() {
        return millstoHMSH(millsToTargetDate());
    }
}