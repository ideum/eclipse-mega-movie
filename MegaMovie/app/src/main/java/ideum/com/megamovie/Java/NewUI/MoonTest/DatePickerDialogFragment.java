package ideum.com.megamovie.Java.NewUI.MoonTest;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ideum.com.megamovie.R;

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
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,i);
        c.set(Calendar.MINUTE,i1);
        Long mills = c.getTimeInMillis();

        edit.putInt(getString(R.string.test_time_hour),i);
        edit.putInt(getString(R.string.test_time_minute),i1);
        edit.commit();
    }
}
