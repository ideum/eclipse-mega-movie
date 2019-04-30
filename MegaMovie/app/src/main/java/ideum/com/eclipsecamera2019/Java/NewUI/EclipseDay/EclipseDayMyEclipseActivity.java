package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import ideum.com.eclipsecamera2019.Java.Application.MyApplication;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

public class EclipseDayMyEclipseActivity extends Activity {

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_my_eclipse);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

    }

    private void next() {
        if (checkEquipmentSelected()) {
            ((MyApplication) getApplication()).currentFragment = EclipseDayEquipmentIntroFragment.class;
        } else {
            ((MyApplication) getApplication()).currentFragment = EclipseDayNoEquipmentFragment.class;
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }


    private boolean checkEquipmentSelected() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
       return !prefs.getString("user_mode_preference","Phone only").equals("Phone only");
    }
}
