package ideum.com.megamovie.Java;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import ideum.com.megamovie.R;

public class UserInfoActivity extends AppCompatActivity {


    public void chooseLocationOnMapButtonPressed(View view) {
        startActivity(new Intent(this,MapActivity.class));
    }

    public void chooseLocationWithAddressButtonPressed(View view) {
        Toast.makeText(getApplicationContext(),"choose with address option",Toast.LENGTH_SHORT).show();
    }

    public void chooseLocationWithGPSCoordinates(View view) {
        Toast.makeText(getApplicationContext(),"choose with gps option",Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
    }
}
