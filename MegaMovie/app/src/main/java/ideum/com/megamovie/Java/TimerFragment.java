package ideum.com.megamovie.Java;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ideum.com.megamovie.R;


public class TimerFragment extends Fragment {
    private static long TIMER_DURATION = 1000*60*60*24*365;
    private TextView mTextView;
    // The data we are counting down to.
    private long targetDateMills;
    public void setTargetDateMills(long mills) {
        targetDateMills = mills;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    private String getTime() {
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);

        return String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
    }

    private long millsecToTargetDate() {
        Calendar rightNow = Calendar.getInstance();


        return targetDateMills - rightNow.getTimeInMillis();
    }

    private String millsToDHMS(long mills) {
        long days = TimeUnit.MILLISECONDS.toDays(mills);
        mills -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(mills);
        mills = mills - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mills);
        mills = mills - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mills);

        String result = String.format("%02d",days);
        result += ":" + String.format("%02d",hours) ;
        result += ":" + String.format("%02d",minutes) ;
        result += ":" + String.format("%02d",seconds) ;

        return result;
    }

    private String timeToSunset() {
        return millsToDHMS(millsecToTargetDate());
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = (TextView) view.findViewById(R.id.timer_text_view);
//        Calendar sunset = Calendar.getInstance();
//        sunset.set(Calendar.HOUR,6);
//        sunset.set(Calendar.MINUTE,0);
//        sunset.set(Calendar.SECOND,0);
//
//        targetDateMills = sunset.getTimeInMillis();


        new CountDownTimer(TIMER_DURATION, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextView.setText("Time until leave work: " + timeToSunset());
            }

            public void onFinish() {
                mTextView.setText("done!");
            }
        }.start();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
