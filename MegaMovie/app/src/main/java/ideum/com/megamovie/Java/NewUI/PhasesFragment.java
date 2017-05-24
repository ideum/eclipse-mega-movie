package ideum.com.megamovie.Java.NewUI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ButtonBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        View rootView = inflater.inflate(R.layout.fragment_phases, container, false);
        Button learnMoreFirstContact = (Button) rootView.findViewById(R.id.learn_more_first_contact);
        learnMoreFirstContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFirstContactInfo();
            }
        });

        return rootView;
    }

    private void displayFirstContactInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("First contact is the beginning of the eclipse, when the moon first makes \"contact\" with the sun." +
                " While the sun may appear dimmer, you still should not view it without safety equipment.\n ")
                .setTitle("First Contact");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
