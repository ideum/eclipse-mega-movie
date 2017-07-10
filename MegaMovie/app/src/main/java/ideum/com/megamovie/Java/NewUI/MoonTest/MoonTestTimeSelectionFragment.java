package ideum.com.megamovie.Java.NewUI.MoonTest;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.NewUI.CalibrateDirectionTestActivity;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonTestTimeSelectionFragment extends Fragment
implements DialogInterface.OnDismissListener,
        CustomNamable{

    private Button chooseTimeButton;

    public MoonTestTimeSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_moon_test_time_selection, container, false);


        chooseTimeButton = rootView.findViewById(R.id.choose_time_button);
        chooseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        Button chooseDateButton = rootView.findViewById(R.id.choose_date_button);
        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        Button nextButton = rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
//                    mainActivity.loadActivity(CalibrateDirectionTestActivity.class);
                    mainActivity.loadFragment(MoonTestCalibrationFragment.class);
                }
            }
        });

                updateUI();

        // Set Moon as default target for the test
        setTestTarget(Planet.Moon);

        // Set method 1 as default
        setCalibrationMethod(1);

        RadioButton sunButton = rootView.findViewById(R.id.sun_radio_button);
        sunButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setTestTarget(Planet.Sun);
                }
            }
        });

        RadioButton moonButton = rootView.findViewById(R.id.moon_radio_button);
        moonButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setTestTarget(Planet.Moon);
                }
            }
        });

        RadioButton method1Button = rootView.findViewById(R.id.method_1_radio_button);
        method1Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setCalibrationMethod(1);
                }
            }
        });

        RadioButton method2Button = rootView.findViewById(R.id.method_2_radio_button);
        method2Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    setCalibrationMethod(2);
                }
            }
        });

        return rootView;
    }

    private void setTestTarget(Planet planet) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        String targetName = planet.name();
        editor.putString(getString(R.string.sun_moon_test_target),targetName);
        editor.commit();
    }

    private void setCalibrationMethod(int method) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.calibration_method),method);
        editor.commit();
    }

    private void updateUI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int hours = prefs.getInt(getContext().getString(R.string.test_time_hour),-1);
        int minutes = prefs.getInt(getContext().getString(R.string.test_time_minute),-1);
        if (hours == -1 || minutes == -1) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hours);
        c.set(Calendar.MINUTE,minutes);
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        chooseTimeButton.setText(formatter.format(c.getTime()));
    }

    private void showTimePickerDialog() {
        TimerPickerDialogFragment dialog = new TimerPickerDialogFragment();
        dialog.addDismissListener(this);
        dialog.show(getChildFragmentManager(),"timePicker");
    }

    private void showDatePickerDialog() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        updateUI();
    }

    @Override
    public String getTitle() {
        return "Moon Test";
    }
}
