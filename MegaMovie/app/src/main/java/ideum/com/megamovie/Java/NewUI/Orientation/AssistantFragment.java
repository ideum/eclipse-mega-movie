package ideum.com.megamovie.Java.NewUI.Orientation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.NewUI.EclipseInfoFragment;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class AssistantFragment extends Fragment
implements CustomNamable{



    public AssistantFragment() {
        // Required empty public constructor
    }

    public static AssistantFragment newInstance() {
        AssistantFragment fragment = new AssistantFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_assistant, container, false);

        Button onlyMyPhoneButton = (Button) rootView.findViewById(R.id.only_my_phone_button);
        onlyMyPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                try {
                    mainActivity.loadAssistantFragment(0);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        Button withEquipmentButton = (Button) rootView.findViewById(R.id.with_equipment_button);
        withEquipmentButton.setOnClickListener(new View.OnClickListener() {
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

        Button withDSLRButton = (Button) rootView.findViewById(R.id.DSLR_camera_button);
        withDSLRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                try {
                    mainActivity.loadAssistantFragment(2);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

//        Button back = (Button) rootView.findViewById(R.id.back_button);
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MainActivity mainActivity = (MainActivity) getActivity();
//                mainActivity.loadFragment(OrientationIntroFragment.class);
//            }
//        });

        Button finish = (Button) rootView.findViewById(R.id.finish_button);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportActionBar().setTitle(getString(R.string.eclipse_info_section_title));
                mainActivity.loadFragment(EclipseInfoFragment.class);
            }
        });


        return rootView;
    }


    @Override
    public String getTitle() {
        return "Orientation";
    }


}
