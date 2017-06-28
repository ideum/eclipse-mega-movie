package ideum.com.megamovie.Java.NewUI.MoonTest;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.CameraControl.CameraFragment;
import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.CameraControl.CaptureSequence;
import ideum.com.megamovie.Java.CameraControl.CaptureSequenceSession;
import ideum.com.megamovie.Java.CameraControl.ConfigParser;
import ideum.com.megamovie.R;

public class MoonTestCaptureActivity extends AppCompatActivity
implements CaptureSequenceSession.CameraController,
        CameraFragment.CaptureListener {

    private static final int CONFIG_ID = R.xml.moon_test_config;
    private CameraPreviewAndCaptureFragment cameraFragment;
    private MyTimer mTimer;
    private CaptureSequenceSession mSession;
    private static final long SESSION_LENGTH_SECONDS = 1;

    private TextView progressTextView;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_capture);

        android.app.FragmentManager fragmentManager = getFragmentManager();
        cameraFragment = (CameraPreviewAndCaptureFragment) fragmentManager.findFragmentById(R.id.camera_fragment);
        cameraFragment.addCaptureListener(this);

        Button takePhotoButton = (Button) findViewById(R.id.take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraFragment.captureStillImage();
            }
        });
        takePhotoButton.setVisibility(View.GONE);
        progressTextView = (TextView) findViewById(R.id.capture_progress_text_view);

        setUpCaptureSequenceSession();
    }

    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }

        progressTextView.setText(String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }

    @Override
    protected void onResume() {
        setUpCaptureSequenceSession();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onPause();
    }

    private Long getStartTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int hour = prefs.getInt(getString(R.string.moon_test_hour),-1);
        int minute = prefs.getInt(getString(R.string.moon_test_minute),-1);
        if (hour == -1 || minute == -1) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);

        return c.getTimeInMillis();
    }

    private void setUpCaptureSequenceSession() {
        try {

            Long startTime = getStartTime();
            if (startTime == null) {
                Toast.makeText(this,"No test scheduled",Toast.LENGTH_SHORT).show();
                return;
            }
            Long duration = SESSION_LENGTH_SECONDS * 1000;

            Resources resources = getResources();
            ConfigParser parser = new ConfigParser(resources, CONFIG_ID);
            CaptureSequence.IntervalProperties properties = parser.getIntervalProperties().get(0);
            CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(properties,startTime,duration);
            CaptureSequence sequence = new CaptureSequence(interval);
            totalNumCaptures = sequence.getRequestQueue().size();
            updateCaptureTextView();
            mSession = new CaptureSequenceSession(sequence, this);


            mTimer = new MyTimer();
            mTimer.addListener(mSession);
            mTimer.startTicking();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        cameraFragment.setCameraSettings(settings);
        cameraFragment.captureStillImage();
    }

    @Override
    public void onCapture() {
        numCaptures += 1;
        updateCaptureTextView();
    }

}
