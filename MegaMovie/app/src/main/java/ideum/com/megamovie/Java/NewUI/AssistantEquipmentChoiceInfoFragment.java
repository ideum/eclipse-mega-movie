package ideum.com.megamovie.Java.NewUI;


import android.content.Context;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import ideum.com.megamovie.R;


public class AssistantEquipmentChoiceInfoFragment extends Fragment
implements AdapterView.OnItemSelectedListener{


    private int index;

    private int imageId;
    private String headerText;
    private String subheaderText;
    private String bodyText;
    private boolean isTerminal;
    private Class nextClass;

    private ImageView imageView;
    private TextView header;
    private TextView subheader;
    private TextView body;
    private Button back;
    private Button forward;


    public AssistantEquipmentChoiceInfoFragment() {
        // Required empty public constructor
    }


    static public AssistantEquipmentChoiceInfoFragment newInstance(int index) {
        AssistantEquipmentChoiceInfoFragment aopf = new AssistantEquipmentChoiceInfoFragment();

        aopf.index = index;
        return aopf;
    }

    private void setValues() throws ClassNotFoundException {

        headerText = getResources().getStringArray(R.array.assistant_headers)[index];
        subheaderText = getResources().getStringArray(R.array.assistant_subheaders)[index];
        bodyText = getResources().getStringArray(R.array.assistant_bodies)[index];
        String imageName= getResources().getStringArray(R.array.assistant_image_names)[index];
        imageId = getResources().getIdentifier(imageName,"drawable",getActivity().getPackageName());
        int isTerminalInt = getResources().getIntArray(R.array.assistant_is_terminals)[index];
        isTerminal = false;
        if (isTerminalInt == 1) {
            isTerminal = true;
        }

        String className = getResources().getStringArray(R.array.assistant_next_classes)[index];
        nextClass = Class.forName(className);
    }

    private void setViews() {
        imageView.setImageResource(imageId);
        header.setText(headerText);
        subheader.setText(subheaderText);
        body.setText(bodyText);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(AssistantFragment.class);
            }
        });

        if (isTerminal) {
            forward.setText("finish");
        } else {
            forward.setText("next");
        }

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(nextClass);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(AssistantFragment.class);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_assistant_equipment_choice_info_fragment, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.image);
        header = (TextView) rootView.findViewById(R.id.header);
        subheader = (TextView) rootView.findViewById(R.id.subheader);
        body = (TextView) rootView.findViewById(R.id.body);
        back = (Button) rootView.findViewById(R.id.back_button);
        forward = (Button) rootView.findViewById(R.id.finish_button);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.equipment_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.user_mode_preferences_entries,android.R.layout.simple_spinner_item);
        spinner.setOnItemSelectedListener(this);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(index);

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        index = position;
        try {
            setValues();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        setViews();
        setUserModePreferences(position);
    }

    private void setUserModePreferences(int index) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String userModePreference = getResources().getStringArray(R.array.user_mode_preferences_entries)[index];

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("user_mode_preference",userModePreference);
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
