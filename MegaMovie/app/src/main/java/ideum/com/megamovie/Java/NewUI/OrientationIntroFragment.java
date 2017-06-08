package ideum.com.megamovie.Java.NewUI;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.R;

public class OrientationIntroFragment extends Fragment {



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_orientation_intro,container,false);
         MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().hide();

        Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(AssistantFragment.class);
                mainActivity.getSupportActionBar().show();
                mainActivity.getSupportActionBar().setTitle(getString(R.string.orientation_section_title));
            }
        });
        return rootView;
    }
}
