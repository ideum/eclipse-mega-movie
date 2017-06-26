package ideum.com.megamovie.Java.NewUI.MoonTest;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonTestTimeChooserFragment extends Fragment {


    public MoonTestTimeChooserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_moon_test_time_chooser, container, false);
    }

}
