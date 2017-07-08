package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class MoonTestCalibrateDirectionActivity extends AppCompatActivity {

    private CalibrateDirectionFragment mCalibrateDirectionFragment;
    private CameraPreviewAndCaptureFragment mCameraFragment;
    private Button nextButton;
    private int state = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_calibrate_direction);


        mCalibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
//        mCalibrateDirectionFragment.shouldUseCurrentTime = false;
//        Long testTime = getMoonTestTimeMills();
//        if (testTime == null) {
//            Toast.makeText(this,"Please set a time for the moon test!",Toast.LENGTH_SHORT).show();
//        } else {
//            mCalibrateDirectionFragment.setTargetTimeMills(getMoonTestTimeMills());
//        }
        mCalibrateDirectionFragment.setTarget(Planet.Moon);
//        mCalibrateDirectionFragment.showView(false);

        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_preview_fragment);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              onNextButtonPressed();
            }
        });

    }

    private void onNextButtonPressed() {
        if (state == 0) {
            nextButton.setText("Finish");
            mCalibrateDirectionFragment.showView(true);
            useCurrentTime(null);
            state = 1;

            useTargetTime(null);

//            android.app.FragmentManager fragmentManager = getFragmentManager();
//            android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.remove(mCameraFragment);
//            transaction.commit();


        } else if (state == 1) {
            finish();
        }
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

    public void calibrateToMoon(View view) {
        mCalibrateDirectionFragment.calibrateModelToMoon();
    }

    public void useCurrentTime(View view) {
        mCalibrateDirectionFragment.shouldUseCurrentTime = true;
    }

    public void useTargetTime(View view) {
        Long targetTime = getMoonTestTimeMills();
        mCalibrateDirectionFragment.setTargetTimeMills(targetTime);

        mCalibrateDirectionFragment.shouldUseCurrentTime = false;
    }

    private void setTargetSun(View view) {
        mCalibrateDirectionFragment.setTarget(Planet.Moon);
    }

    private void setTargetMoon(View view) {
        mCalibrateDirectionFragment.setTarget(Planet.Sun);
    }
}
