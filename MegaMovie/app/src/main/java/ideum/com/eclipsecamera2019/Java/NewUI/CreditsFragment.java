package ideum.com.eclipsecamera2019.Java.NewUI;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.eclipsecamera2019.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreditsFragment extends Fragment {


    public CreditsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_credits, container, false);
//        Button ideum_learn_more_button = (Button) rootView.findViewById(R.id.ideum_learn_more);
//        ideum_learn_more_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openWebPage(getString(R.string.ideum_url));
//            }
//        });

//        Button ssl_learn_more_button = (Button) rootView.findViewById(R.id.ssl_learn_more);
//        ssl_learn_more_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openWebPage(getString(R.string.ssl_url));
//            }
//        });

        View ideum_card = (View) rootView.findViewById(R.id.ideum_credit_card);
        ideum_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(getString(R.string.ideum_url));
            }
        });

        View ssl_card = (View) rootView.findViewById(R.id.ssl_credit_card);
        ssl_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(getString(R.string.ssl_url));
            }
        });
        View eaa_card = (View) rootView.findViewById(R.id.eaa_credit_card);
        eaa_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(getString(R.string.eaa_url));
            }
        });

        return rootView;
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
