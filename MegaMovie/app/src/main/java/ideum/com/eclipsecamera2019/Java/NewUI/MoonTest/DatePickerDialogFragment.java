package ideum.com.eclipsecamera2019.Java.NewUI.MoonTest;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ideum.com.eclipsecamera2019.R;

/**
 * Created by MT_User on 7/10/2017.
 */

public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener{

    private List<DialogInterface.OnDismissListener> listeners = new ArrayList<>();
    public void addDismissListener(DialogInterface.OnDismissListener listener) {
        listeners.add(listener);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),this,year,month,dayOfMonth);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        for(DialogInterface.OnDismissListener listener : listeners) {
            listener.onDismiss(dialog);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt(getString(R.string.test_time_year),i);
        edit.putInt(getString(R.string.test_time_month),i1);
        edit.putInt(getString(R.string.test_time_day_of_month),i2);
        edit.commit();
    }
}
