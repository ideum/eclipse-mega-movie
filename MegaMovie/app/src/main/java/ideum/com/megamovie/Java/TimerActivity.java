package ideum.com.megamovie.Java;

import android.icu.util.Calendar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ideum.com.megamovie.R;

public class TimerActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        TimerFragment fragment = (TimerFragment) getFragmentManager().findFragmentById(R.id.timer_fragment);
        Calendar sunset = Calendar.getInstance();
        sunset.set(Calendar.HOUR,6);
        sunset.set(Calendar.MINUTE,0);
        sunset.set(Calendar.SECOND,0);

        fragment.setTargetDateMills(sunset.getTimeInMillis());

    }
}
