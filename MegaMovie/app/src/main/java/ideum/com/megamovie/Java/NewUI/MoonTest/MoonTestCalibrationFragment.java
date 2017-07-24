package ideum.com.megamovie.Java.NewUI.MoonTest;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.NewUI.CalibrateDirectionTestActivity;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonTestCalibrationFragment extends Fragment
implements CustomNamable{


    public MoonTestCalibrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_moon_test_calibration, container, false);
        Button calibrateDirectionButton = rootView.findViewById(R.id.calibrate_direction_button);

        calibrateDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.loadActivity(MoonTestCalibrateDirectionActivity.class);
                }
            }
        });

        Button calibrateLensButton = rootView.findViewById(R.id.calibrate_lens_button);
        calibrateLensButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.loadActivity(MoonTestCalibrateLensActivity.class);
                }
            }
        });

        Button nextButton = rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.loadActivity(MoonTestCaptureActivity.class);
                }
            }
        });
        return rootView;
    }


//    private boolean checkLensPreference() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        int lensPref = preferences.getInt(getString(R.string.lens_magnification_pref_key),0);
//        return lensPref != 0;
//    }

    @Override
    public String getTitle() {
        return "Calibration";
    }
}
