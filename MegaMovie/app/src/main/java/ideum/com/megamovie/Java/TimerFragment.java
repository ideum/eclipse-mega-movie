package ideum.com.megamovie.Java;

import android.app.Activity;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Date;

import ideum.com.megamovie.R;


public class TimerFragment extends Fragment {
    private TextView mTextView;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = (TextView) view.findViewById(R.id.timer_text_view);

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
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
