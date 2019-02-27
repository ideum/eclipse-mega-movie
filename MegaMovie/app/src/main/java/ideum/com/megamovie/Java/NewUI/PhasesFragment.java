package ideum.com.megamovie.Java.NewUI;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;



import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes;
import ideum.com.megamovie.R;

import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c1;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c3;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c4;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.cm;

public class PhasesFragment extends Fragment {

    private TextView c1TextView;
    private TextView c2TextView;
    private TextView cmTextView;
    private TextView c3TextView;
    private TextView c4TextView;

    private TextView learnMorec1;
    private TextView learnMorec2;
    private TextView learnMorecm;
    private TextView learnMorec3;
    private TextView learnMorec4;

    private boolean c1Expanded = false;
    private boolean c2Expanded = false;
    private boolean cmExpanded = false;
    private boolean c3Expanded = false;
    private boolean c4Expanded = false;

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
        final View rootView = inflater.inflate(R.layout.fragment_phases, container, false);

        final LinearLayout intro_layout = (LinearLayout) rootView.findViewById(R.id.phases_intro);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean gotItVisible = settings.getBoolean(getString(R.string.phases_got_it_visible_key), true);


        if (gotItVisible) {
            intro_layout.setVisibility(View.VISIBLE);
            Button got_it = (Button) rootView.findViewById(R.id.got_it);
            got_it.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout bottom = (LinearLayout) rootView.findViewById(R.id.bottom_layout);
                    bottom.animate().translationY(-intro_layout.getHeight());
                    intro_layout.animate().alpha(0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(getString(R.string.phases_got_it_visible_key), false);
                    editor.commit();
                }
            });
        }

        c1TextView = (TextView) rootView.findViewById(R.id.c1_text_view);
        c2TextView = (TextView) rootView.findViewById(R.id.c2_text_view);
        cmTextView = (TextView) rootView.findViewById(R.id.cm_text_view);
        c3TextView = (TextView) rootView.findViewById(R.id.c3_text_view);
        c4TextView = (TextView) rootView.findViewById(R.id.c4_text_view);

        learnMorec1 = (TextView) rootView.findViewById(R.id.learn_more_c1);
        learnMorec2 = (TextView) rootView.findViewById(R.id.learn_more_c2);
        learnMorecm = (TextView) rootView.findViewById(R.id.learn_more_cm);
        learnMorec3 = (TextView) rootView.findViewById(R.id.learn_more_c3);
        learnMorec4 = (TextView) rootView.findViewById(R.id.learn_more_c4);


        View c1View = rootView.findViewById(R.id.c1_view);
        c1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(c1);
            }
        });

        View c2View = rootView.findViewById(R.id.c2_view);
        c2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(c2);
            }
        });


        View cmView = rootView.findViewById(R.id.cm_view);
        cmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(cm);
            }
        });
        View c3View = rootView.findViewById(R.id.c3_view);
        c3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(c3);
            }
        });

        View c4View = rootView.findViewById(R.id.c4_view);
        c4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(c4);
            }
        });

        return rootView;
    }

    public void setContactTimes(Long[] times) {
        if ((times[0] != c1Mills) || (times[1] != c2Mills) || (times[2] != cmMills) || (times[3] != c3Mills) || (times[4] != c4Mills)) {
            c1Mills = times[0];
            c2Mills = times[1];
            cmMills = times[2];
            c3Mills = times[3];
            c4Mills = times[4];
            updateUi();
        }
    }

    private void onLearnMoreTapped(EclipseTimes.Phase event) {
        String caption = "";
        String title = "";
        // Todo: refactor this
        switch (event) {
            case c1:
                caption = getResources().getString(R.string.first_contact_dialog_message);
                title = getResources().getString(R.string.first_contact);
                break;
            case c2:
                caption = getResources().getString(R.string.second_contact_dialog_message);
                title = getResources().getString(R.string.second_contact);
                break;
            case cm:
                caption = getResources().getString(R.string.mid_eclipse_dialog_message);
                title = getResources().getString(R.string.mid_eclipse);
                break;
            case c3:
                caption = getResources().getString(R.string.third_contact_dialog_message);
                title = getResources().getString(R.string.third_contact);
                break;
            case c4:
                caption = getResources().getString(R.string.fourth_contact_dialog_message);
                title = getResources().getString(R.string.fourth_contact);

                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(caption)
                .setTitle(title)
                .setPositiveButton(getResources().getString(R.string.got_it), null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

    }



    public void updateUi() {
        if (!isAdded()) {
            return;
        }
        String s1 = getResources().getString(R.string.first_contact) + ": " + getContactTimeString(c1Mills);
        String s2 = getResources().getString(R.string.second_contact) + ": " + getContactTimeString(c2Mills);
        String sm = getResources().getString(R.string.mid_eclipse) + ": " + getContactTimeString(cmMills);
        String s3 = getResources().getString(R.string.third_contact) + ": " + getContactTimeString(c3Mills);
        String s4 = getResources().getString(R.string.fourth_contact) + ": " + getContactTimeString(c4Mills);


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


    private String getTimeZoneId() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return settings.getString(getActivity().getString(R.string.timezone_id), "");
    }

    public String getContactTimeString(Long mills) {

        if (mills == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String timeZoneId = getTimeZoneId();
        String timeZoneDisplayName = "";
        if (timeZoneId != null) {
            formatter.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            timeZoneDisplayName = TimeZone.getTimeZone(timeZoneId).getDisplayName();// getDisplayName(true, TimeZone.SHORT, Locale.US);
        }


        return formatter.format(calendar.getTime()) + " " + timeZoneDisplayName;
    }

}
