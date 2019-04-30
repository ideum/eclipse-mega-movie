package ideum.com.eclipsecamera2019.Java.NewUI.Orientation;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import ideum.com.eclipsecamera2019.Java.Application.CustomNamable;
import ideum.com.eclipsecamera2019.Java.NewUI.EclipseInfoFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AssistantEquipmentChoiceFragment extends Fragment
        implements AdapterView.OnItemSelectedListener,
        CustomNamable,
NumberPicker.OnValueChangeListener{

    public AssistantEquipmentChoiceFragment() {
        // Required empty public constructor
    }

    private NumberPicker lensPicker;

    //private Spinner lensSpinner;
    private Spinner tripodSpinner;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_assistant_equipment_choice, container, false);


        Button finish = (Button) rootView.findViewById(R.id.finish_button);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportActionBar().setTitle(getString(R.string.eclipse_info_section_title));
                mainActivity.loadFragment(EclipseInfoFragment.class);
            }
        });

//        lensSpinner = (Spinner) rootView.findViewById(R.id.lens_spinner);
//        ArrayAdapter<CharSequence> lensAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.lens_choices_entries,android.R.layout.simple_spinner_item);
//
//        lensAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        lensSpinner.setAdapter(lensAdapter);
//        lensSpinner.setOnItemSelectedListener(this);

        lensPicker = (NumberPicker) rootView.findViewById(R.id.lens_picker);
        lensPicker.setMinValue(0);
        lensPicker.setMaxValue(50);

        lensPicker.setOnValueChangedListener(this);

         tripodSpinner = (Spinner) rootView.findViewById(R.id.tripod_spinner);
        ArrayAdapter<CharSequence> tripodAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.tripod_choices,android.R.layout.simple_spinner_item);

        tripodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripodSpinner.setAdapter(tripodAdapter);
        tripodSpinner.setOnItemSelectedListener(this);

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        if (parent == lensSpinner) {
//            setLensPreference(position);
//        }
        if (parent == tripodSpinner) {
            setTripodPreference(position);
        }
    }

    private void setLensPreference(int index) {
        String lensChoice = getResources().getStringArray(R.array.lens_choices_entries)[index];
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lens_preference",lensChoice);
        editor.commit();
    }

    private void setTripodPreference(int index) {
        String lensChoice = getResources().getStringArray(R.array.tripod_choices)[index];
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("tripod_preference",lensChoice);
        editor.commit();
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public int getTitleId() {
        return R.string.orientation_section_title;
    }




    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        setLensMagnificationPref(newVal);
    }

    private void setLensMagnificationPref(int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(getString(R.string.lens_magnification_pref_key),value);
        editor.commit();
    }
}
