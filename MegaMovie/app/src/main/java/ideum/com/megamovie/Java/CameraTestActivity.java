package ideum.com.megamovie.Java;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ideum.com.megamovie.R;

public class CameraTestActivity extends AppCompatActivity {

    private CameraPreviewAndCaptureFragment mCameraFragment;
    private TextView mSensitivityTextView;
    private TextView mFocusDistanceTextView;
    private TextView mDurationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_test_fragment);
        mSensitivityTextView = (TextView) findViewById(R.id.sensitivity_text_view);
        mFocusDistanceTextView = (TextView) findViewById(R.id.focusDistance_text_view);
        mDurationTextView = (TextView) findViewById(R.id.duration_text_view);
        updateTextViews();
    }
    private void updateTextViews(){
        mSensitivityTextView.setText("Sensitivity: " + String.valueOf(mCameraFragment.mSensorSensitivity));
        mFocusDistanceTextView.setText("Focus: " + String.valueOf(mCameraFragment.mFocusDistance));
        mDurationTextView.setText("Duration: " + String.valueOf(mCameraFragment.mDuration));
    }

    public void increaseSensitivity(View view) {
        mCameraFragment.incrementSensitivity(30);
        updateTextViews();
    }

    public void decreaseSensitivity(View view) {
        mCameraFragment.decrementSensitivity(30);
        updateTextViews();
    }

    public void increaseFocusDistance(View view) {
        mCameraFragment.incrementFocusDistance(0.5f);
        updateTextViews();
    }
    public void decreaseFocusDistance(View view) {
        mCameraFragment.decrementFocusDistance(0.5f);
        updateTextViews();
    }
    public void increaseDuration(View view) {
        mCameraFragment.incrementDuration(1);
        updateTextViews();
    }

    public void decreaseDuration(View view) {
        mCameraFragment.decrementDuration(1);
        updateTextViews();
    }


    public void captureImage(View view) {
        new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
        }

            public void onFinish() {
                takePhoto();
            }
        }.start();
    }

    private void takePhoto() {
        Toast.makeText(getApplicationContext(),"Photo taken!",Toast.LENGTH_SHORT).show();
        mCameraFragment.captureStillImage();
    }
}

