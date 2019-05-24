package ideum.com.eclipsecamera2019.Java.NewUI.MoonTest;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.eclipsecamera2019.Java.Application.CustomNamable;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

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


    @Override
    public int getTitleId() {
        return R.string.calibration_section_title;
    }
}
