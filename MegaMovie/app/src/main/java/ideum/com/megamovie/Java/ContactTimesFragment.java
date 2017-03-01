/**
 * UI Fragment for displaying eclipse contact times
 */

package ideum.com.megamovie.Java;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.megamovie.R;


public class ContactTimesFragment extends Fragment {

    private EclipseTimeCalculator mEclipseTimeCalculator;
    private TextView textViewContact2;
    private TextView textViewContact3;
    private LocationProvider mLocationProvider;

    public void setLocationProvider(LocationProvider provider) {
        mLocationProvider = provider;
    }

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

        textViewContact2 = (TextView) view.findViewById(R.id.contact2);
        textViewContact3 = (TextView) view.findViewById(R.id.contact3);

        updateTextViews();
    }

    public void updateTextViews() {
        if (mEclipseTimeCalculator == null || mLocationProvider == null) {
            return;
        }

        Location location = mLocationProvider.getLocation();
        if (location == null) {
            return;
        }
        Long contact2 = mEclipseTimeCalculator.getEclipseTime(EclipseTimeCalculator.Event.CONTACT2);
        Long contact3 = mEclipseTimeCalculator.getEclipseTime(EclipseTimeCalculator.Event.CONTACT3);

        String contact2String = "";
        if (contact2 != null) {
            contact2String = timeOfDayString(contact2);
        }
        String contact3String = "";
        if (contact3 != null) {
            contact3String = timeOfDayString(contact3);
        }

        textViewContact2.setText("Contact2: " + contact2String);
        textViewContact3.setText("Contact3: " + contact3String);
    }

    private String timeOfDayString(Long mills) {
        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formatter.format(calendar.getTime());
    }


}
