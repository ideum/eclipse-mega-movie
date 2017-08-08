package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.megamovie.R;

public class EclipseDayPointingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_pointing);

        Button captureModeButton = (Button) findViewById(R.id.capture_mode_button);
        captureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCaptureMode();
            }
        });
    }

    public void goToCaptureMode() {
        Intent intent = new Intent(this,EclipseDayCaptureActivity.class);
        startActivity(intent);
    }
}
