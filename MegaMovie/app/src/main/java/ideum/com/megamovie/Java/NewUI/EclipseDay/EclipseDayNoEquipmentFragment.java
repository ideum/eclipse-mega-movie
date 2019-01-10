package ideum.com.megamovie.Java.NewUI.EclipseDay;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class EclipseDayNoEquipmentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_eclipse_day_no_equipment, container, false);
        Button captureModeButton = rootView.findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCaptureMode();
            }
        });

        return rootView;
    }

    public void enterCaptureMode() {
        MainActivity activity = (MainActivity) getActivity();
        activity.loadActivity(EclipseDayCaptureActivity.class);
    }
}
