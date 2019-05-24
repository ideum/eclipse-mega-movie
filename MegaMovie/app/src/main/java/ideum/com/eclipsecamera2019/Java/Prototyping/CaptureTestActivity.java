package ideum.com.eclipsecamera2019.Java.Prototyping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequence;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceBuilderDummy;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceSession;
import ideum.com.eclipsecamera2019.Java.CameraControl.IVideoAndStillCamera;
import ideum.com.eclipsecamera2019.Java.CameraControl.VideoFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.GPSFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.eclipsecamera2019.Java.OrientationController.Clock;
import ideum.com.eclipsecamera2019.R;

public class CaptureTestActivity extends AppCompatActivity
        implements CaptureSequenceSession.CaptureSessionListener,
        CaptureSequenceSession.CaptureSessionCompletionListerner,
        MyTimer.MyTimerListener {

    private IVideoAndStillCamera mVideoFragment;
    private SmallCountdownFragment mCountdownFragment;
    private CaptureSequenceSession mSession;
    private MyTimer mTimer;
    private TextView photosTakenTextView;
    private int numCaptures = 0;
    private int totalNumCaptures = 0;
    private GPSFragment mGPSFragment;
    private Clock timeProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_test);
        mVideoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment);
        mVideoFragment.setDirectoryName("eclipse camera 2019 test images");
        mCountdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        photosTakenTextView = findViewById(R.id.photo_taken_textview);
        findViewById(R.id.start_sequence_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSequence();
            }
        });
        mGPSFragment = new GPSFragment();
        timeProvider = new Clock() {
            @Override
            public long getTimeInMillisSinceEpoch() {
                return Calendar.getInstance().getTimeInMillis();
            }
        };

        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
    }

    private void startSequence() {
        setUpCaptureSequenceSession(timeProvider.getTimeInMillisSinceEpoch() + 1000);
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.startTicking();
    }

    private void setUpCaptureSequenceSession(long startTime) {
        CaptureSequence sequence = CaptureSequenceBuilderDummy.makeVideoTestSequence(startTime);
        if (sequence == null) {
            return;
        }
        if (mSession != null) {
            mSession.stop();
        }
        mSession = new CaptureSequenceSession(sequence, this,timeProvider);
        totalNumCaptures = sequence.numberCapturesRemaining();
        numCaptures = 0;
        updateCaptureTextView();

        mSession.addListener(this);
        mSession.start();

    }

    private void updateCaptureTextView() {
        photosTakenTextView.setText("Photos Taken: " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }

    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        Log.d("CaptureTest", "take photo");
        mVideoFragment.takePhotoWithSettings(settings);
        numCaptures++;
        updateCaptureTextView();
    }

    @Override
    public void startRecordingVideo(CaptureSequence.CaptureSettings settings) {
        Log.d("CaptureTest", "start video");
        mVideoFragment.startRecordingVideo(settings);
    }

    @Override
    public void stopRecordingVideo() {
        Log.d("CaptureTest", "stop video");
        mVideoFragment.stopRecordingVideo();
        numCaptures++;
        updateCaptureTextView();
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {
        mTimer.cancel();
        Toast.makeText(this, "Sequence Completed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTick() {
        if (mSession != null) {
            mSession.onTick();
        }
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mSession != null) {
            mSession.stop();
            mSession = null;
        }
        super.onPause();
    }
}
