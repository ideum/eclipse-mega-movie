package ideum.com.megamovie.Java.NewUI;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ThrowOnExtraProperties;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import ideum.com.megamovie.R;


public class CountdownFragment extends Fragment {

    private TextView daysTextView;
    private TextView hoursTextView;
    private TextView minutesTextView;
    private TextView secondsTextView;
    private TextView distanceToTotalityTextView;

    public CountdownFragment() {
        // Required empty public constructor
    }

    public static CountdownFragment newInstance() {
        CountdownFragment fragment = new CountdownFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_countdown, container, false);
        daysTextView = (TextView) rootView.findViewById(R.id.days_text_view);
        hoursTextView = (TextView) rootView.findViewById(R.id.hours_text_view);
        minutesTextView = (TextView) rootView.findViewById(R.id.minutes_text_view);
        secondsTextView = (TextView) rootView.findViewById(R.id.seconds_text_view);
        distanceToTotalityTextView = (TextView) rootView.findViewById(R.id.distance_to_totality);

        updateDistanceToTotalityTextView(3);

        return rootView;
    }

    public void setDistanceToPathOfTotality(double km) {
        updateDistanceToTotalityTextView(km);
    }

    private void updateDistanceToTotalityTextView(double km) {
        String text = "";
        if (km == 0) {
            text = getString(R.string.in_the_path_of_totality_string);
        } else {
            text = getString(R.string.distance_to_totality_format, (int) km);
        }
        distanceToTotalityTextView.setText(text);
    }

    public void setMillsRemaining(Long mills) {
        setCountdownViews(mills);
    }

    /*
    The argument mills is the time remaining in milliseconds.
     */
    private void setCountdownViews(long mills) {


        long days = TimeUnit.MILLISECONDS.toDays(mills);
        mills -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(mills);
        mills = mills - TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(mills);
        mills = mills - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mills);

        if(daysTextView != null) {
            daysTextView.setText(String.format("%02d", days));
        }
        if (hoursTextView != null) {
            hoursTextView.setText(String.format("%02d", hours));
        }
        if (minutesTextView != null) {
            minutesTextView.setText(String.format("%02d", minutes));
        }
        if (secondsTextView != null) {
            secondsTextView.setText(String.format("%02d", seconds));
        }
    }


}
