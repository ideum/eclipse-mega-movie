package ideum.com.megamovie.Java;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import ideum.com.megamovie.R;

public class ResultsActivity extends AppCompatActivity {

    public void viewResults(View view) {
        Toast.makeText(getApplicationContext(),"View Results", Toast.LENGTH_SHORT).show();
    }

    public void shareResults(View view) {
        Toast.makeText(getApplicationContext(),"Share Results", Toast.LENGTH_SHORT).show();
    }

    public void back(View view) {
        startActivity(new Intent(this,CameraActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
    }
}
