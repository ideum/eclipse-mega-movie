package ideum.com.megamovie.Java.NewUI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.Java.LocationAndTiming.LocationNotifier;
import ideum.com.megamovie.R;

public class PhasesFragment extends Fragment {

    private TextView c1TextView;
    private TextView c2TextView;
    private TextView cmTextView;
    private TextView c3TextView;
    private TextView c4TextView;

    private Long c1Mills;
    private Long c2Mills;
    private Long cmMills;
    private Long c3Mills;
    private Long c4Mills;

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

        c1TextView = (TextView) rootView.findViewById(R.id.c1_text_view);
        c2TextView = (TextView) rootView.findViewById(R.id.c2_text_view);
        cmTextView = (TextView) rootView.findViewById(R.id.cm_text_view);
        c3TextView = (TextView) rootView.findViewById(R.id.c3_text_view);
        c4TextView = (TextView) rootView.findViewById(R.id.c4_text_view);


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

    public void updateUi() {
        if (!isAdded()) {
            return;
        }
        String s1 = "First Contact: " + timeOfDayString(c1Mills);
        String s2 = "Second Contact: " + timeOfDayString(c2Mills);
        String sm = "Mid Eclipse: " + timeOfDayString(cmMills);
        String s3 = "Third Contact: " + timeOfDayString(c3Mills);
        String s4 = "Fourth Contact: " + timeOfDayString(c4Mills);


        if (c1TextView != null) {
            c1TextView.setText(s1);
        }


        if (c2TextView != null) {
            c2TextView.setText(s2);
        }

        if (cmTextView != null) {
            cmTextView.setText(sm);
        }

        if (c3TextView != null) {
            c3TextView.setText(s3);
        }

        if (c4TextView != null) {
            c4TextView.setText(s4);
        }
    }

    public void setC1Mills(Long mills) {
        c1Mills = mills;
        updateUi();
    }

    public void setC2Mills(Long mills) {

        c2Mills = mills;
        updateUi();
    }

    public void setCmMills(Long mills) {
        cmMills = mills;
        updateUi();
    }

    public void setC3Mills(Long mills) {
        c3Mills = mills;
        updateUi();
    }

    public void setC4Mills(Long mills) {
        c4Mills = mills;
        updateUi();
    }



    private String timeOfDayString(Long mills) {
        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
       // formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return formatter.format(calendar.getTime());
    }

}
