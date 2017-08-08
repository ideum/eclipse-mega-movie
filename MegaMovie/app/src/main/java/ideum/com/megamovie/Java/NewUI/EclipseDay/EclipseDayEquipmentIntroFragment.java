package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class EclipseDayEquipmentIntroFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eclipse_day_equipment_intro, container, false);


        Button getStartedButton = rootView.findViewById(R.id.get_started_button);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCompassFragment();
            }
        });

        Button captureModeButton = rootView.findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCaptureMode();
            }
        });


        return rootView;
    }

    public void loadCompassFragment() {
        ((MainActivity) getActivity()).loadFragment(EclipseDayCompassCalibration.class);
    }

    public void goToCaptureMode() {
        ((MainActivity) getActivity()).loadActivity(EclipseDayCaptureActivity.class);
    }
}
