package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class EclipseDayIntroFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eclipse_day_intro, container, false);

        final Button getStarted = rootView.findViewById(R.id.get_started_button);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getStarted();
            }
        });

        return rootView;
    }


    private void getStarted() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.loadActivity(EclipseDayMyEclipseActivity.class);
    }
}
