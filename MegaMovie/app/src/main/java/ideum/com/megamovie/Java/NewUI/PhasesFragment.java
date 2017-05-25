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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.first_contact_dialog_message))
                        .setTitle("First Contact");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Button learnMoreSecondContact = (Button) rootView.findViewById(R.id.learn_more_second_contact);
        learnMoreSecondContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.second_contact_dialog_message))
                        .setTitle("Second Contact");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Button learnMoreThirdContact = (Button) rootView.findViewById(R.id.learn_more_third_contact);
        learnMoreThirdContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.third_contact_dialog_message))
                        .setTitle("Third Contact");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Button learnMoreFourthContact = (Button) rootView.findViewById(R.id.learn_more_fourth_contact);
        learnMoreFourthContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.fourth_contact_dialog_message))
                        .setTitle("Fourth Contact");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return rootView;
    }

}
