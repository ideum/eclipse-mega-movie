package ideum.com.megamovie.Java.NewUI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class CalibrateDirectionTestActivity extends AppCompatActivity
implements SensorEventListener{

    private CameraPreviewAndCaptureFragment mCameraFragment;
    private SensorManager mSensorManager;
    private Sensor mMagneticField;

    private TextView compassTextView;
    private CalibrateDirectionFragment calibrateDirectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_direction_test);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

         calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.shouldUseCurrentTime = true;
        calibrateDirectionFragment.setTarget(Planet.Moon);

        android.app.FragmentManager fragmentManager = getFragmentManager();
        mCameraFragment = (CameraPreviewAndCaptureFragment) fragmentManager.findFragmentById(R.id.camera_preview_fragment);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        compassTextView = (TextView) findViewById(R.id.accuracy_text_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void dim(View view) {
        if (mCameraFragment != null) {
            mCameraFragment.decrementDuration(10);

        }
    }

    public void brighten(View view) {
        if (mCameraFragment != null) {
            mCameraFragment.incrementDuration(10);
        }
    }

    public void setTargetSun(View view) {
        calibrateDirectionFragment.setTarget(Planet.Sun);
    }

    public void setTargetMoon(View view) {
        calibrateDirectionFragment.setTarget(Planet.Moon);
    }

    public void useCurrentTime(View view) {
        calibrateDirectionFragment.setShouldUseCurrentTime(true);
    }

    public void resetCalibration(View view) {
        calibrateDirectionFragment.resetModelCalibration();
    }

    public void useMethod1(View view) {
        calibrateDirectionFragment.useMethod(1);
    }

    public void useMethod2(View view) {
        calibrateDirectionFragment.useMethod(2);
    }

    public void useTargetTime(View view) {
        Long targetTime = getMoonTestTimeMills();
        calibrateDirectionFragment.setTargetTimeMills(targetTime);

        calibrateDirectionFragment.setShouldUseCurrentTime(false);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    public void calibrateToMoon(View view) {
        calibrateDirectionFragment.calibrateModelToMoon();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        String s = "can't tell accuracy";
        if (i == mSensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            s = "accuracy high";
        } else if (i == mSensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            s = "accuracy low";
        } else if (i == mSensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            s = "accuracy medium";
        } else if (i == mSensorManager.SENSOR_STATUS_UNRELIABLE) {
            s = "accuracy unreliable";
        }

       compassTextView.setText(s);
    }

    private Long getMoonTestTimeMills() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int hour = preferences.getInt(getString(R.string.moon_test_hour),-1);
        int minute = preferences.getInt(getString(R.string.moon_test_minute),-1);
        if (hour == -1 || minute == -1) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }
}
