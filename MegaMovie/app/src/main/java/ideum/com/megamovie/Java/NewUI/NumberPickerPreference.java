package ideum.com.megamovie.Java.NewUI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import ideum.com.megamovie.R;

/**
 * Created by MT_User on 6/15/2017.
 */

public class NumberPickerPreference extends DialogPreference {


    NumberPicker picker;
    Integer initialValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_pref);
        setPositiveButtonText(context.getString(R.string.ok));
        setNegativeButtonText(context.getString(R.string.cancel));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.picker = (NumberPicker)view.findViewById(R.id.pref_num_picker);
        // TODO this should be an XML parameter:
        picker.setMinValue(0);
        picker.setMaxValue(50);
        if ( this.initialValue != null ) {
            picker.setValue(initialValue);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if ( which == DialogInterface.BUTTON_POSITIVE ) {
            this.initialValue = picker.getValue();
            persistInt( initialValue );
            callChangeListener( initialValue );
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        int def = ( defaultValue instanceof Number ) ? (Integer)defaultValue
                : ( defaultValue != null ) ? Integer.parseInt(defaultValue.toString()) : 1;
        if ( restorePersistedValue ) {
            this.initialValue = getPersistedInt(def);
        }
        else this.initialValue = (Integer)defaultValue;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }
}
