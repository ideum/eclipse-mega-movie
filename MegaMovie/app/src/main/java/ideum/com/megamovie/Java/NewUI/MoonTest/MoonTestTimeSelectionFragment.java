package ideum.com.megamovie.Java.NewUI.MoonTest;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.LocationAndTiming.DateUtil;
import ideum.com.megamovie.Java.NewUI.CalibrateDirectionTestActivity;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonTestTimeSelectionFragment extends Fragment
        implements DialogInterface.OnDismissListener,
        CustomNamable {

    private static final int LEAD_TIME_MINUTES = 0;

    private Button chooseTimeButton;
    private Button chooseDateButton;

    public MoonTestTimeSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_moon_test_time_selection, container, false);


        deselectTargetPreference();

        chooseTimeButton = rootView.findViewById(R.id.choose_time_button);
        chooseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        chooseDateButton = rootView.findViewById(R.id.choose_date_button);
        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });



        updateUI();

//        // Set Moon as default target for the test
//        setTestTarget(Planet.Moon);

        // Set method 1 as default
        setCalibrationMethod(1);



        Button sunButton = rootView.findViewById(R.id.choose_sun_button);
        sunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTestTarget(Planet.Sun);
                showSunTestAlert();
                onNextButtonPressed();
            }
        });

        Button moonButton = rootView.findViewById(R.id.choose_moon_button);
        moonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTestTarget(Planet.Moon);
                onNextButtonPressed();
            }
        });

        return rootView;
    }

    private String timeToTestString() {
        Date tDate = getTargetDateFromSettings();
        if (tDate == null) {
            return null;
        }
        return DateUtil.countdownStringToDate(tDate);
    }

    private void onNextButtonPressed() {
        if (!checkTimeSet()) {
            showTimeNotSetAlert();
            return;
        }
        if (!checkTargetSet()) {
            showTargetNotSetAlert();
            return;
        }

        if (!checkTimeGreaterThanLead()) {
            showNeedMoreLeadTimeAlert();
            return;
        }


        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.loadFragment(MoonTestCalibrationFragment.class);
        }
    }

    private void showTimeNotSetAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Please set a time and date for the test")
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNeedMoreLeadTimeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Please schedule your test at least 5 minutes in the future.")
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTargetNotSetAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Please select either the sun or moon as a target.")
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSunTestAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.sun_test_warning))
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setTestTarget(Planet planet) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        String targetName = planet.name();
        editor.putString(getString(R.string.sun_moon_test_target), targetName);
        editor.commit();
    }

    private boolean checkTargetSet() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String planetName = preferences.getString(getString(R.string.sun_moon_test_target),"");
        if (planetName.equals(Planet.Moon.name()) || planetName.equals(Planet.Sun.name()) ) {
            return true;
        }

        return false;
    }

    private void setCalibrationMethod(int method) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.calibration_method), method);
        editor.commit();
    }

    private void deselectTargetPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.sun_moon_test_target),"");
        editor.commit();
    }

    private void updateUI() {


        String dateString = targetDateStringFromSettings();
        if (dateString != null) {
            chooseDateButton.setText(dateString);
        } else {
            chooseDateButton.setText("Select");
        }

        String timeString = targetTimeStringFromSettings();
        if (timeString != null) {
            chooseTimeButton.setText(timeString);
        } else {
            chooseTimeButton.setText("Select");
        }
    }

    private Date getTargetDateFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int year = prefs.getInt(getString(R.string.test_time_year), -1);
        int month = prefs.getInt(getString(R.string.test_time_month), -1);
        int dayOfMonth = prefs.getInt(getString(R.string.test_time_day_of_month), -1);
        int hours = prefs.getInt(getContext().getString(R.string.test_time_hour), -1);
        int minutes = prefs.getInt(getContext().getString(R.string.test_time_minute), -1);

        if (year == -1
                || month == -1
                || dayOfMonth == -1
                || hours == -1
                || minutes == -1) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);

        return c.getTime();
    }

    private String targetTimeStringFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int hours = prefs.getInt(getContext().getString(R.string.test_time_hour), -1);
        int minutes = prefs.getInt(getContext().getString(R.string.test_time_minute), -1);
        if (hours == -1 || minutes == -1) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 0);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 0);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm a");
        return dateFormatter.format(c.getTime());

    }

    private boolean checkTimeGreaterThanLead() {
        Date target = getTargetDateFromSettings();
        long targetMills = target.getTime();

        long currentMills = Calendar.getInstance().getTimeInMillis();

        long intervalMills = targetMills - currentMills;

        long intervalMinutes = intervalMills/(1000 * 60);
        return intervalMinutes >= LEAD_TIME_MINUTES;


    }

    private String targetDateStringFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int year = prefs.getInt(getString(R.string.test_time_year), -1);
        int month = prefs.getInt(getString(R.string.test_time_month), -1);
        int dayOfMonth = prefs.getInt(getString(R.string.test_time_day_of_month), -1);
        if (year == -1 || month == -1 || dayOfMonth == -1) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd");
        return dateFormatter.format(c.getTime());

    }


    private boolean checkTimeSet() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean yearSet = prefs.getInt(getString(R.string.test_time_year), -1) != -1;
        boolean monthSet = prefs.getInt(getString(R.string.test_time_month), -1) != -1;
        boolean dayOfMonthSet = prefs.getInt(getString(R.string.test_time_day_of_month), -1) != -1;
        boolean hoursSet = prefs.getInt(getContext().getString(R.string.test_time_hour), -1) != -1;
        boolean minutesSet = prefs.getInt(getContext().getString(R.string.test_time_minute), -1) != -1;

        return yearSet
                && monthSet
                && dayOfMonthSet
                && hoursSet
                && minutesSet;
    }

    private void showTimePickerDialog() {
        TimerPickerDialogFragment dialog = new TimerPickerDialogFragment();
        dialog.addDismissListener(this);
        dialog.show(getChildFragmentManager(), "timePicker");
    }

    private void showDatePickerDialog() {

        DatePickerDialogFragment dialog = new DatePickerDialogFragment();
        dialog.addDismissListener(this);
        dialog.show(getChildFragmentManager(), "datePicker");

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        updateUI();
        String timeToTest = timeToTestString();
        if (timeToTest != null) {
            String message = "Your test is scheduled to occur in " + timeToTestString() + ".";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String getTitle() {
        return "Practice Mode";
    }
}
