package ideum.com.megamovie.Java.NewUI.Orientation;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.NewUI.EclipseInfoFragment;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.Java.NewUI.MyEclipseActivity;
import ideum.com.megamovie.R;


public class AssistantEquipmentChoiceInfoFragment extends Fragment
implements AdapterView.OnItemSelectedListener,
        CustomNamable{


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
   // private Button back;
    private Button forward;
    private Button settings;


    public AssistantEquipmentChoiceInfoFragment() {
        // Required empty public constructor
    }


    static public AssistantEquipmentChoiceInfoFragment newInstance(int index) {
        AssistantEquipmentChoiceInfoFragment aopf = new AssistantEquipmentChoiceInfoFragment();

        aopf.index = index;
        return aopf;
    }

    private void setValues() throws ClassNotFoundException {

        String[] bodyTexts = {
                getResources().getString(R.string.equipment_info_phone),
                "Holding a camera by hand can cause tiny vibrations that blur photos. " +
                        "This can be especially troublesome with astronomical images. Mounting your " +
                        "phone on a tripod is an easy way to improve your photos. There are many kinds " +
                        "of tripods available for cell phones, including inexpensive ones made by Joby, Square Jellyfish, and Charger City." +
                        "<br><br>Also, while your phone may have a great camera, images of the sun are likely to " +
                        "be small unless you use an external lens like those made by Apexel, Hsini, and Neewer. " +
                        "We recommend using a lens with a magnification of at least 20x, although the app is compatible with any lens. " +
                        "Lenses like these are very portable and can help you take great photos of many things in addition to the eclipse." +
                        "<br><br>If you’re using an especially high-powered lens, you might consider using a tracking-mount. A relatively " +
                        "compact and inexpensive choice is the Star Adventurer made by Sky-Watcher." +
                        "<br><br>Once you’ve decided on the equipment you’ll use, go to My Eclipse settings to enter your choice (you can change it at any time)."
                ,
                " If you want to use a separate camera to photograph the eclipse, you can still use the eclipse map and countdown " +
                        "functions in the Megamovie Mobile app." +
                        "<br><br>You may also be interested in being an official volunteer to capture images for the Megamovie itself. " +
                        "To learn more, visit the <a href=\"https://eclipsemega.movie/\">Megamovie Website</a>."};


        headerText = getResources().getStringArray(R.array.assistant_headers)[index];
        subheaderText = getResources().getStringArray(R.array.assistant_subheaders)[index];
        bodyText = bodyTexts[index];
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

        body.setText(Html.fromHtml(bodyText));
        body.setMovementMethod(LinkMovementMethod.getInstance());


        if (isTerminal) {
            forward.setText("finish");
        } else {
            forward.setText("next");
        }

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(EclipseInfoFragment.class);
            }
        });

        if (index != 1) {
            settings.setVisibility(View.GONE);
        } else {
            settings.setVisibility(View.VISIBLE);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(new Intent(getActivity(),MyEclipseActivity.class));
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_assistant_equipment_choice_info_fragment, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.image);
        header = (TextView) rootView.findViewById(R.id.header);
        subheader = (TextView) rootView.findViewById(R.id.subheader);
        body = (TextView) rootView.findViewById(R.id.body);
        //body.setText(getResources().getString(R.string.equipment_info_dslr));


        //back = (Button) rootView.findViewById(R.id.back_button);
        forward = (Button) rootView.findViewById(R.id.finish_button);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.equipment_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.user_mode_preferences_entries,android.R.layout.simple_spinner_item);
        spinner.setOnItemSelectedListener(this);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(index);

        settings = (Button) rootView.findViewById(R.id.go_to_settings);


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

    @Override
    public String getTitle() {
        return "Orientation";
    }


}
