package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.R;

public class MoonTestCalibrateLensActivity extends AppCompatActivity {

    private CameraPreviewAndCaptureFragment mCameraFragment;
    private int sensitivityInterval = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_calibrate_lens);

        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_preview_fragment);

        Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void dim(View view) {
        if (mCameraFragment != null) {
            mCameraFragment.decrementSensitivity(sensitivityInterval);
        }
    }

    public void brighten(View view) {
        if (mCameraFragment != null) {
            mCameraFragment.incrementSensitivity(sensitivityInterval);
        }
    }
}
