
package ideum.com.megamovie.Java;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ideum.com.megamovie.R;

public class CaptureActivity extends AppCompatActivity {

    private final static String TAG = "CaptureActivity";
    private static int TIMER_LENGTH = 300;
    private static int TIMER_INTERVAL = 100;
    private static long SENSOR_EXPOSURE_TIME = 9516;//5 * 1000000;
    private static int SENSOR_SENSITIVITY = 60;
    private static float LENS_FOCUS_DISTANCE = 0;
    private CameraFragment mCameraFragment;

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this,CalibrationActivity.class));
    }
    public void loadResultsActivity(View view) {
        startActivity(new Intent(this,ResultsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        mCameraFragment = new CameraFragment();

        getFragmentManager().beginTransaction().add(
                android.R.id.content, mCameraFragment).commit();
        Log.e(TAG,"Fragment added");
    }

    public void startCaptureSequence(View view) {
        startTimer();
    }


    public void startTimer() {
        new CountDownTimer(TIMER_LENGTH, TIMER_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                mCameraFragment.takePhoto(SENSOR_EXPOSURE_TIME,SENSOR_SENSITIVITY,LENS_FOCUS_DISTANCE);
            }

            public void onFinish() {
                    Toast.makeText(getApplicationContext(), "done!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }
}
