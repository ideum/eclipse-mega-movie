package ideum.com.eclipsecamera2019.Java.Prototyping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequence;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceBuilderDummy;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceSession;
import ideum.com.eclipsecamera2019.Java.CameraControl.VideoFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.eclipsecamera2019.R;

public class CaptureTestActivity extends AppCompatActivity
        implements CaptureSequenceSession.CameraController,
        CaptureSequenceSession.CaptureSessionCompletionListerner,
        MyTimer.MyTimerListener {

    private VideoFragment mVideoFragment;
    private SmallCountdownFragment mCountdownFragment;
    private CaptureSequenceSession mSession;
    private MyTimer mTimer;
    private TextView photosTakenTextView;
    private int numCaptures = 0;
    private int totalNumCaptures = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_test);
        mVideoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment);
        mCountdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        photosTakenTextView = findViewById(R.id.photo_taken_textview);
        findViewById(R.id.start_sequence_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSequence();
            }
        });
    }

    private void startSequence() {
        setUpCaptureSequenceSession(Calendar.getInstance().getTimeInMillis() + 1000);
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
        mSession = new CaptureSequenceSession(sequence, this);
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
        if (settings.isVideo) {

        } else {

            mVideoFragment.takePhotoWithSettings(settings);
            numCaptures++;
            updateCaptureTextView();
        }
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {
        mTimer.cancel();
        Toast.makeText(this, "Sequence Completed!", Toast.LENGTH_SHORT).show();
    }

//

    @Override
    public void onTick() {
        if (mSession != null) {
            mSession.onTick();
        }
    }

    //    @Override
//    protected void onResume() {
//        super.onResume();
//        mTimer = new MyTimer();
//        mTimer.addListener(this);
//        mTimer.startTicking();
//    }
//
    @Override
    protected void onPause() {
        mTimer.cancel();
        if (mSession != null) {
            mSession.stop();
            mSession = null;
        }
        super.onPause();
    }
}
