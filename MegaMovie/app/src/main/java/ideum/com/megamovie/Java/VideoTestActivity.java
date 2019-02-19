package ideum.com.megamovie.Java;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.R;

public class VideoTestActivity extends AppCompatActivity {
    private CameraPreviewAndCaptureFragment cameraFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);

        cameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);


    }
}
