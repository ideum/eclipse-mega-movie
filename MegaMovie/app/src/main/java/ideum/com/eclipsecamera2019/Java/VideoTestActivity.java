package ideum.com.eclipsecamera2019.Java;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import ideum.com.eclipsecamera2019.Java.CameraControl.VideoFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MoonTest.TimerPickerDialogFragment;
import ideum.com.eclipsecamera2019.R;

public class VideoTestActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    private VideoFragment mVideoFragment;
    private boolean isRecording;
    private ImageView isRecordingImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        mVideoFragment = VideoFragment.newInstance();

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container,mVideoFragment )
                    .commit();
        }

        isRecordingImage = findViewById(R.id.is_recording_button);

        findViewById(R.id.start_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        findViewById(R.id.stop_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        findViewById(R.id.choose_time_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private void showTimePickerDialog() {
        PreciseTimePickerDialogFragment dialog = new PreciseTimePickerDialogFragment();
        dialog.addDismissListener(this);
        dialog.show(getSupportFragmentManager(),"time picker dialogue");
    }


    public void startRecording() {
        if(mVideoFragment != null) {
            mVideoFragment.tryToStartRecording();
        }
        isRecording = true;
        updateUI();
    }

    public void stopRecording() {
        if(mVideoFragment != null) {
            mVideoFragment.tryToStopRecording();
        }
        isRecording = false;
        updateUI();
    }

    private void updateUI() {
        if(isRecording) {
            isRecordingImage.setImageResource(R.drawable.ic_radio_button_checked_green_24dp);
        } else {
            isRecordingImage.setImageResource(R.drawable.ic_radio_button_unchecked_green_24dp);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }
}
