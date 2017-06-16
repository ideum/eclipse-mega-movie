package ideum.com.megamovie.Java.NewUI;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.Java.LocationAndTiming.LocationNotifier;
import ideum.com.megamovie.R;

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
        boolean gotItVisible = settings.getBoolean(getString(R.string.phases_got_it_visible_key),true);


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
                    editor.putBoolean(getString(R.string.phases_got_it_visible_key),false);
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
                onLearnMoreTapped(EclipseTimingMap.Event.CONTACT1);
            }
        });

        View c2View = rootView.findViewById(R.id.c2_view);
        c2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(EclipseTimingMap.Event.CONTACT2);
            }
        });


        View cmView = rootView.findViewById(R.id.cm_view);
        cmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(EclipseTimingMap.Event.MIDDLE);
            }
        });
        View c3View = rootView.findViewById(R.id.c3_view);
        c3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(EclipseTimingMap.Event.CONTACT3);
            }
        });

        View c4View =  rootView.findViewById(R.id.c4_view);
        c4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLearnMoreTapped(EclipseTimingMap.Event.CONTACT4);
            }
        });

        return rootView;
    }

    private void onLearnMoreTapped(EclipseTimingMap.Event event) {
        String caption = "";
        // Todo: refactor this
        switch (event) {
            case CONTACT1:
                caption = getResources().getString(R.string.first_contact_dialog_message);
                if (!c1Expanded) {
                    learnMorec1.setText(caption);
                } else {
                    learnMorec1.setText("LEARN MORE");
                }
                c1Expanded = !c1Expanded;
                break;

            case CONTACT2:
                caption = getResources().getString(R.string.second_contact_dialog_message);
                if (!c2Expanded) {
                    learnMorec2.setText(caption);
                } else {
                    learnMorec2.setText("LEARN MORE");
                }
                c2Expanded = !c2Expanded;
                break;
            case MIDDLE:
                caption = getResources().getString(R.string.mid_eclipse_dialog_message);
                if (!cmExpanded) {
                    learnMorecm.setText(caption);
                } else {
                    learnMorecm.setText("LEARN MORE");
                }
                cmExpanded = !cmExpanded;
                break;
            case CONTACT3:
                caption = getResources().getString(R.string.third_contact_dialog_message);
                if (!c3Expanded) {
                    learnMorec3.setText(caption);
                } else {
                    learnMorec3.setText("LEARN MORE");
                }
                c3Expanded = !c3Expanded;
                break;
            case CONTACT4:
                caption = getResources().getString(R.string.fourth_contact_dialog_message);
                if (!c4Expanded) {
                    learnMorec4.setText(caption);
                } else {
                    learnMorec4.setText("LEARN MORE");
                }
                c4Expanded = !c4Expanded;
                break;
        }

    }

    public void setContactTimeString(EclipseTimingMap.Event event, String timeOfDayString) {
        switch (event) {
            case CONTACT1:
                if (c1TextView != null) {
                    c1TextView.setText("First Contact: " + timeOfDayString);
                }
            case CONTACT2:
                if (c2TextView != null) {
                    c2TextView.setText("Second Contact: " + timeOfDayString);
                }
            case MIDDLE:
                if (cmTextView != null) {
                    cmTextView.setText("Mid Eclipse: " + timeOfDayString);
                }
            case CONTACT3:
                if (c3TextView != null) {
                    c3TextView.setText("Third Contact: " + timeOfDayString);
                }
            case CONTACT4:
                if (c4TextView != null) {
                    c4TextView.setText("Fourth Contact: " + timeOfDayString);
                }

        }
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

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));


        return formatter.format(calendar.getTime());
    }

}
