package ideum.com.megamovie.Java;

import android.content.Context;
import android.net.Uri;
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

import ideum.com.megamovie.R;


public class ContactTimesFragment extends Fragment {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    private TextView textViewContact1;
    private TextView textViewContact2;
    private TextView textViewContact3;
    private TextView textViewContact4;

    public ContactTimesFragment() {
        // Required empty public constructor
    }

    public void setEclipseTimeCalculator(EclipseTimeCalculator calculator) {
        mEclipseTimeCalculator = calculator;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_contact_times, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        textViewContact1 = (TextView) view.findViewById(R.id.contact1);
        textViewContact2 = (TextView) view.findViewById(R.id.contact2);
        textViewContact3 = (TextView) view.findViewById(R.id.contact3);
        textViewContact4 = (TextView) view.findViewById(R.id.contact4);

        updateTextViews();
    }

    public void updateTextViews() {
        if (mEclipseTimeCalculator == null) {
            return;
        }
        long contact1 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT1);
        long contact2 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT2);
        long contact3 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT3);
        long contact4 = mEclipseTimeCalculator.dummyEclipseTime(EclipseTimeCalculator.Event.CONTACT4);

        textViewContact1.setText("C1: " + timeOfDayString(contact1));
        textViewContact2.setText("C2: " + timeOfDayString(contact2));
        textViewContact3.setText("C3: " + timeOfDayString(contact3));
        textViewContact4.setText("C4: " + timeOfDayString(contact4));
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
