package ideum.com.megamovie.Java;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.R;

public class VideoTestActivity extends AppCompatActivity {
    private CameraPreviewAndCaptureFragment cameraFragment;
    private boolean isRecording;
    private Button videoButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);

        cameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);

        Button takePhotoButton = (Button) findViewById(R.id.take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TakePhoto
                        ();
            }
        });

         videoButton = (Button) findViewById(R.id.video_button);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnVideoButtonPressed();
            }
        });

        updateUI();
    }

    private void TakePhoto() {
        if(cameraFragment != null) {
            cameraFragment.takePhoto();
        }
    }

    private void OnVideoButtonPressed() {
        if(isRecording) {
            isRecording = false;
        } else {
            isRecording = true;
        }
        updateUI();
    }

    private void updateUI() {
        if(isRecording) {
            videoButton.setText("Stop Recording");
        } else {
            videoButton.setText("Start Recording");
        }

    }
}
