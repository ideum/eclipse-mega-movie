package ideum.com.eclipsecamera2019.Java;
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

import ideum.com.eclipsecamera2019.R;
public class PreciseTimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


    public interface OnDismissListener {
        void onDismiss(int hour,int minute);
    }
    private int hour;
    private int minute;
    private List<OnDismissListener> listeners = new ArrayList<>();
    public void addDismissListener(OnDismissListener listener) {
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
        hour = i;
        minute = i1;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        for(OnDismissListener listener : listeners) {
            listener.onDismiss(hour,minute);
        }
    }
}
