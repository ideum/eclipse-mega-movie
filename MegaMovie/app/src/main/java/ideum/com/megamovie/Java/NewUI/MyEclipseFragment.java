package ideum.com.megamovie.Java.NewUI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import ideum.com.megamovie.R;



public class MyEclipseFragment extends PreferenceFragment {

    private static Preference user_mode_preference;
    private static Preference lens_preference;
    private static Preference tripod_preference;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if(preference.equals(tripod_preference)) {
               showNoTripodWarning(preference,value);
            }

            if (preference.getKey().equals("user_mode_preference")) {
                if (!stringValue.equals("Phone with equipment")) {
                    lens_preference.setEnabled(false);
                    tripod_preference.setEnabled(false);
                } else {
                    lens_preference.setEnabled(true);
                    tripod_preference.setEnabled(true);
                }
            }
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void showNoTripodWarning(Preference preference,Object value) {
        String stringValue = value.toString();
        if (!stringValue.equals("true")) {
            Context context = preference.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getResources().getString(R.string.no_tripod_warning))
                    .setPositiveButton("Got It", null)
                    .setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

//    private void showTripodWarning() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(getResources()
//                .getString(R.string.safety_warning))
//                .setTitle(getResources().getString(R.string.safety_warning_title))
//                .setPositiveButton("Got It",null)
//                .setCancelable(false);
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        String preferenceValueString = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        if (preference instanceof NumberPickerPreference) {
            NumberPickerPreference npp = (NumberPickerPreference) preference;
            int value = sharedPreferences.getInt(preference.getKey(),0);
            preferenceValueString = String.valueOf(value);
        } else if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference cbp = (CheckBoxPreference) preference;
            boolean value = sharedPreferences.getBoolean(preference.getKey(),false);
            preferenceValueString = String.valueOf(value);
        }
        else {
            preferenceValueString = sharedPreferences.getString(preference.getKey(),"");
        }


        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preferenceValueString);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.my_eclipse);

        user_mode_preference = findPreference("user_mode_preference");
        lens_preference = findPreference(getString(R.string.lens_magnification_pref_key));
        tripod_preference = findPreference("tripod_preference");

        bindPreferenceSummaryToValue(user_mode_preference);
        bindPreferenceSummaryToValue(lens_preference);
        bindPreferenceSummaryToValue(tripod_preference);
    }
}
