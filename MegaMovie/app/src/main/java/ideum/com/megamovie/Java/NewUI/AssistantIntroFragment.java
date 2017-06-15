package ideum.com.megamovie.Java.NewUI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.R;

public class AssistantIntroFragment extends Fragment
implements CustomNamable{


    public AssistantIntroFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assistant_intro, container, false);
    }

    @Override
    public String getTitle() {
        return "Orientation";
    }

    @Override
    public boolean shouldShowActionBar() {
        return false;
    }
}
