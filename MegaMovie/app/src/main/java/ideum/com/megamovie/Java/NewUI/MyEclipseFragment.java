package ideum.com.megamovie.Java.NewUI;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import ideum.com.megamovie.R;



public class MyEclipseFragment extends PreferenceFragment {

    private static Preference user_mode_preference;
    private static Preference lens_preference;
    private static Preference tripod_preference;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
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

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
        .getDefaultSharedPreferences(preference.getContext())
        .getString(preference.getKey(),""));

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.my_eclipse);

        user_mode_preference = findPreference("user_mode_preference");
        lens_preference = findPreference("lens_preference");
        tripod_preference = findPreference("tripod_preference");

        bindPreferenceSummaryToValue(user_mode_preference);
        bindPreferenceSummaryToValue(lens_preference);
        bindPreferenceSummaryToValue(tripod_preference);
    }
}
