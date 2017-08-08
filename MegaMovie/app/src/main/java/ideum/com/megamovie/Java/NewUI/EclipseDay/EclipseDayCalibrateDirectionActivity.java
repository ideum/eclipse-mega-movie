package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.megamovie.R;

public class EclipseDayCalibrateDirectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_calibrate_direction);

        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPointingActivity();
            }
        });

    }

    public void loadPointingActivity() {
        Intent intent = new Intent(this,EclipseDayPointingActivity.class);
        startActivity(intent);
    }
}
