package ideum.com.megamovie.Java.NewUI;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AssistantEquipmentFragment extends Fragment {


    public AssistantEquipmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assistant_equipment, container, false);
    }

}
