package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class MoonTestCalibrateDirectionActivity extends AppCompatActivity {

    private CalibrateDirectionFragment mCalibrateDirectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_calibrate_direction);

        Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCalibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        Long testTime = getMoonTestTimeMills();
        if (testTime == null) {
            Toast.makeText(this,"Please set a time for the moon test!",Toast.LENGTH_SHORT).show();
        } else {
            mCalibrateDirectionFragment.setTargetTimeMills(getMoonTestTimeMills());
        }
        mCalibrateDirectionFragment.setTarget(Planet.Moon);

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
