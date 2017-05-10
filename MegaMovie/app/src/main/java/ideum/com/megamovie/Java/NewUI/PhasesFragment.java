package ideum.com.megamovie.Java.NewUI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.megamovie.R;

public class PhasesFragment extends Fragment {

    public PhasesFragment() {
        // Required empty public constructor
    }

    public static PhasesFragment newInstance() {
        PhasesFragment fragment = new PhasesFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phases, container, false);
    }

}
