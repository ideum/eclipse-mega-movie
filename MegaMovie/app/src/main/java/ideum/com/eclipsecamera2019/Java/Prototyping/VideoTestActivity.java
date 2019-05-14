package ideum.com.eclipsecamera2019.Java.Prototyping;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.LocationSource;

import ideum.com.eclipsecamera2019.Java.CameraControl.VideoFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.GPSFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.eclipsecamera2019.Java.Prototyping.PreciseTimePickerDialogFragment;
import ideum.com.eclipsecamera2019.R;

import java.util.Calendar;

public class VideoTestActivity extends AppCompatActivity
        implements PreciseTimePickerDialogFragment.OnDismissListener,
        MyTimer.MyTimerListener, LocationSource.OnLocationChangedListener {
    private VideoFragment mVideoFragment;
    private boolean isRecording;
    private ImageView isRecordingImage;
    private boolean readyToRecord;
    private SmallCountdownFragment countdownFragment;
    private MyTimer mTimer;
    private GPSFragment mGPSFragment;
    private long recordingElapsedTimeMillis = 0;
    private long recordingTotalTimeMillis = 5000;
    private long timeOffset;

    private TextView recordingTextView;
    private TextView durationTextView;
    private Button snapPhotoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        mVideoFragment = VideoFragment.newInstance();

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mVideoFragment)
                    .commit();
        }
        Log.i("VideoTest", "Starting up");
        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.activate(this);
        countdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        isRecordingImage = findViewById(R.id.is_recording_button);
        recordingTextView = findViewById(R.id.recording_text);
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
                showTimePickerDialog();
            }
        });

//        findViewById(R.id.incremement_duration_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                long newDuration = mVideoFragment.incrementDuration(5);
//                durationTextView.setText(String.valueOf(newDuration) + " ms");
//            }
//        });
//
//        findViewById(R.id.decrement_duration_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                long newDuration = mVideoFragment.decrementDuration(5);
//                durationTextView.setText(String.valueOf(newDuration) + " ms");
//            }
//        });

        durationTextView = findViewById(R.id.duration_text_view);
        snapPhotoButton = findViewById(R.id.snap_photo_button);
        snapPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoFragment.takePhoto();
            }
        });
    }

    private void showTimePickerDialog() {
        PreciseTimePickerDialogFragment dialog = new PreciseTimePickerDialogFragment();
        dialog.addDismissListener(this);
        dialog.show(getSupportFragmentManager(), "time picker dialogue");
        stopCountdown();
//        Calendar c = Calendar.getInstance();
//        targetTime = c.getTimeInMillis() + 5000;
//
//
//        startCountdown();
    }


    public void startRecording() {
        if (mVideoFragment != null) {
            mVideoFragment.tryToStartRecording();
        }
        isRecording = true;
        updateUI();
    }

    public void stopRecording() {
        if (mVideoFragment != null) {
            mVideoFragment.tryToStopRecording();
        }
        isRecording = false;
        updateUI();
    }

    private void updateUI() {
        if (isRecording) {
            isRecordingImage.setImageResource(R.drawable.ic_radio_button_checked_green_24dp);
            recordingTextView.setText("RECORDING");
        } else {
            isRecordingImage.setImageResource(R.drawable.ic_radio_button_unchecked_green_24dp);
            recordingTextView.setText("NOT RECORDING");
        }
        snapPhotoButton.setEnabled(!isRecording);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private long targetTime;
    private long timeRemaining;
    private void startCountdown() {
        readyToRecord = true;
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.startTicking();
    }

    private void stopCountdown() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(isRecording) {
            stopRecording();
        }
    }

    @Override
    public void onDismiss(int hour, int minute) {
        if (hour == 0 && minute == 0) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND,0);
        targetTime = c.getTimeInMillis();


        startCountdown();
    }

    @Override
    public void onTick() {
        timeRemaining = targetTime - correctedTimeMillis();
        countdownFragment.setTimeRemainingMillis(timeRemaining);
        if (readyToRecord && timeRemaining < 0) {
            recordingElapsedTimeMillis = 0;
            startRecording();
            readyToRecord = false;
        }
        if(isRecording) {
           recordingElapsedTimeMillis = correctedTimeMillis() - targetTime;
           if (recordingElapsedTimeMillis > recordingTotalTimeMillis) {
               stopRecording();
              stopCountdown();
           }
        }
    }


    private long correctedTimeMillis() {
        return Calendar.getInstance().getTimeInMillis() + timeOffset;
    }

    @Override
    public void onLocationChanged(Location location) {
        long systemTime = Calendar.getInstance().getTimeInMillis();
        long gpsTime = location.getTime();
        timeOffset = gpsTime - systemTime;
    }
}
