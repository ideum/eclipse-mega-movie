package ideum.com.eclipsecamera2019.Java.NewUI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.eclipsecamera2019.R;


public class AboutEclipseAppFragment extends Fragment {

    public AboutEclipseAppFragment() {
        // Required empty public constructor
    }

    public static AboutEclipseAppFragment newInstance() {
        AboutEclipseAppFragment fragment = new AboutEclipseAppFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_eclipse_app, container, false);

    }
}