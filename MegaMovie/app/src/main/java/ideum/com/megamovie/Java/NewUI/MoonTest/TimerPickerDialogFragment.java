package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ideum.com.megamovie.R;

/**
 * Created by MT_User on 6/26/2017.
 */

public class TimerPickerDialogFragment extends DialogFragment
implements TimePickerDialog.OnTimeSetListener{

    private List<DialogInterface.OnDismissListener> listeners = new ArrayList<>();
    public void addDismissListener(DialogInterface.OnDismissListener listener) {
        listeners.add(listener);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt(getString(R.string.test_time_hour),i);
        edit.putInt(getString(R.string.test_time_minute),i1);
        edit.commit();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        for(DialogInterface.OnDismissListener listener : listeners) {
            listener.onDismiss(dialog);
        }
    }
}
