package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class EclipseDayMyEclipseActivity extends Activity {

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_my_eclipse);

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
