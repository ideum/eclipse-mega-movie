package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.Java.NewUI.MoonTest.MoonTestCalibrateLensActivity;
import ideum.com.eclipsecamera2019.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EclipseDayCalibrateEquimentFragment extends Fragment {


    public EclipseDayCalibrateEquimentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_eclipse_day_calibrate_equiment, container, false);

        Button captureModeButton = rootView.findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).loadActivity(EclipseDayCaptureActivity.class);
            }
        });

        Button calibrateDirectionButton = rootView.findViewById(R.id.calibrate_direction_button);
        calibrateDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCalibrateDirectionActivity();
            }
        });

        Button calibrateLensButton = rootView.findViewById(R.id.calibrate_lens_button);
        calibrateLensButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCalibrateLensActivity();
            }
        });



        return rootView;
    }

    public void loadCalibrateDirectionActivity() {
        ((MainActivity)getActivity()).loadActivity(EclipseDayCalibrateDirectionActivity.class);
    }

    public void loadCalibrateLensActivity() {
        ((MainActivity)getActivity()).loadActivity(MoonTestCalibrateLensActivity.class);
    }

}
