package ideum.com.megamovie.Java.NewUI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AssistantEquipmentChoiceFragment extends Fragment {

    public AssistantEquipmentChoiceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_assistant_equipment_choice, container, false);

        Button back = (Button) rootView.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                try {
                    mainActivity.loadAssistantFragment(1);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        Button finish = (Button) rootView.findViewById(R.id.finish_button);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(EclipseInfoFragment.class);
            }
        });

        Spinner lensSpinner = (Spinner) rootView.findViewById(R.id.lens_spinner);
        ArrayAdapter<CharSequence> lensAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.lens_choices,android.R.layout.simple_spinner_item);

        lensAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lensSpinner.setAdapter(lensAdapter);

        Spinner tripodSpinner = (Spinner) rootView.findViewById(R.id.tripod_spinner);
        ArrayAdapter<CharSequence> tripodAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.tripod_choices,android.R.layout.simple_spinner_item);

        tripodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripodSpinner.setAdapter(tripodAdapter);

        return rootView;
    }

}
