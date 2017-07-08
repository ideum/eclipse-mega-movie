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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.NewUI.CalibrateDirectionTestActivity;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonTestTimeSelectionFragment extends Fragment
implements DialogInterface.OnDismissListener,
        CustomNamable{


    private TextView targetTimeTextView;
    private Button chooseTimeButton;

    public MoonTestTimeSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_moon_test_time_selection, container, false);
        targetTimeTextView = rootView.findViewById(R.id.moon_test_target_time_text_view);
        targetTimeTextView.setVisibility(View.GONE);

        chooseTimeButton = rootView.findViewById(R.id.choose_time_button);
        chooseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        Button nextButton = rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.loadActivity(CalibrateDirectionTestActivity.class);
//                    mainActivity.loadFragment(MoonTestCalibrationFragment.class);
                }
            }
        });

                updateUI();


        return rootView;
    }

    private void updateUI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int hours = prefs.getInt(getContext().getString(R.string.moon_test_hour),-1);
        int minutes = prefs.getInt(getContext().getString(R.string.moon_test_minute),-1);
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
