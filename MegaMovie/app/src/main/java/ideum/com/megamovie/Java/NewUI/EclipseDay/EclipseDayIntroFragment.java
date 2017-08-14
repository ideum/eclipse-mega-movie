package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class EclipseDayIntroFragment extends Fragment
implements CustomNamable {



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

        if(checkIfInPath()) {
            Toast.makeText(getActivity(),"You are in the path",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"You are not in the path",Toast.LENGTH_SHORT).show();
        }

        return rootView;


    }

    private boolean checkIfInPath() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getBoolean(getString(R.string.in_path_key),false);
    }


    private void getStarted() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.loadActivity(EclipseDayMyEclipseActivity.class);
    }

    @Override
    public String getTitle() {
        return "Eclipse Image Capture";
    }
}
