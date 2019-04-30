package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EclipseDayCompassCalibration extends Fragment
        implements SensorEventListener {


    private TextView compassAccuracyTextView;

    private SensorManager mSensorManager;
    private Sensor mMagneticField;



    public EclipseDayCompassCalibration() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_compass_calibration, container, false);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        compassAccuracyTextView = rootView.findViewById(R.id.compass_accuracy_text_view);

        Button nextButton = rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.loadFragment(EclipseDayCalibrateEquimentFragment.class);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        String s = getString(R.string.cant_tell_accuracy);
        if (i == mSensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            s = getString(R.string.high).toUpperCase();
        } else if (i == mSensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            s = getString(R.string.low).toUpperCase();
        } else if (i == mSensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            s = getString(R.string.medium).toUpperCase();
        } else if (i == mSensorManager.SENSOR_STATUS_UNRELIABLE) {
            s = getString(R.string.unreliable).toUpperCase();
        }
        String messageStart = getString(R.string.compass_accuracy);

        compassAccuracyTextView.setText(messageStart + ": " + s);
    }

}
