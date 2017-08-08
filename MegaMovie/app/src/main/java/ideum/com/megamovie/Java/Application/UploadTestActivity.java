package ideum.com.megamovie.Java.Application;

import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ideum.com.megamovie.R;

public class UploadTestActivity extends AppCompatActivity {

    private File directory;
    private UploadFragment mUploadFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_test);
        directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Megamovie practice Jul 24 18:35 PM" );
         mUploadFragment = new UploadFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(
                android.R.id.content, mUploadFragment).commit();

        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUploadFragment.uploadFilesInDirectory("Megamovie practice Jul 24 18:35 PM");
            }
        });

    }
}
