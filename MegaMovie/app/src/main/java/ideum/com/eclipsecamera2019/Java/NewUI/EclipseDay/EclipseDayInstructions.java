package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.eclipsecamera2019.R;

public class EclipseDayInstructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_eclipse_day_instructions);

        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
    }

    private void next() {
        Intent intent = new Intent(this, EclipseDayMyEclipseActivity.class);
        startActivity(intent);
    }
}
