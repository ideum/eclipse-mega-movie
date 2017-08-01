package ideum.com.megamovie.Java.NewUI;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import ideum.com.megamovie.R;

public class SensorTestActivity extends AppCompatActivity
implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mAmbientTemp;
    private Sensor mInternalTemp;
    private Sensor mHumidity;
    private Sensor mIlluminance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mAmbientTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mInternalTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mIlluminance = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        mSensorManager.registerListener(this,mInternalTemp,SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this,mAmbientTemp,SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this,mPressure,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mHumidity,SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this,mIlluminance,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float temp = sensorEvent.values[0];
        Log.i("SENSOR",String.valueOf(temp));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
