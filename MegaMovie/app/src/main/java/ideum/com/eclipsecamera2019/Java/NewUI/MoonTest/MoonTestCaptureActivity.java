package ideum.com.eclipsecamera2019.Java.NewUI.MoonTest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ideum.com.eclipsecamera2019.Java.CameraControl.ICameraCaptureListener;
import ideum.com.eclipsecamera2019.Java.CameraControl.IVideoAndStillCamera;
import ideum.com.eclipsecamera2019.Java.CameraControl.VideoFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.DateUtil;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.GPSFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequence;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceSession;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

public class MoonTestCaptureActivity extends AppCompatActivity
        implements CaptureSequenceSession.CaptureSessionListener,
        ICameraCaptureListener,
        MyTimer.MyTimerListener,
        CaptureSequenceSession.CaptureSessionCompletionListerner {

    private static final int CONFIG_ID = R.xml.moon_test_config;
    private IVideoAndStillCamera cameraFragment;
    private MyTimer mTimer;
    private CaptureSequenceSession mSession;
    private static final long SESSION_LENGTH_SECONDS = 360;
    private static final int LEAD_TIME_SECONDS = 180;
    private static final int SPACING_SECONDS = 30;

    private static final long EXPOSURE_TIME = 5000000L;
    private static int SENSOR_SENSITIVITY = 60;
    private static float FOCUS_DISTANCE = 0f;

    private Long targetTimeMills;

    private TextView testTimeTextView;

    private TextView progressTextView;

    private SmallCountdownFragment countdownFragment;
    private Button finishButton;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;
    private GPSFragment mGPSFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_capture);

        // Set portrait mode and keep phone from sleeping
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        targetTimeMills = getTestTimeFromSettings();

        // gps just used for image metadata in practice mode
         mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        cameraFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);

        cameraFragment.setLocationProvider(mGPSFragment);

        cameraFragment.addCaptureListener(this);

        testTimeTextView = (TextView) findViewById(R.id.test_time_text_view);

        progressTextView = (TextView) findViewById(R.id.capture_progress_text_view);
        finishButton = (Button) findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFinishButtonPressed();
            }
        });

        countdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        countdownFragment.setTargetTimeMills(targetTimeMills);
        Long testTime = getTestTimeFromSettings();

        Date testTimeDate = new Date(testTime);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        String testTimeString = getString(R.string.test_time_start) + ": " + timeFormatter.format(testTimeDate);


        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd HH:mm a");


        cameraFragment.setDirectoryName("Megamovie practice " + dateFormatter.format(testTimeDate));

        testTimeTextView.setText(testTimeString);

        int leadTimeMinutes = LEAD_TIME_SECONDS / 60;
        int leadTimeSeconds = LEAD_TIME_SECONDS - 60 * leadTimeMinutes;

    }

    private void onFinishButtonPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        if (mSession == null) {
            setUpCaptureSequenceSession();
        }
        mTimer = new MyTimer();
       // mTimer.addListener(mSession);
        mSession.start();
        mTimer.addListener(this);
        // mTimer.addListener(countdownFragment);
        mTimer.startTicking();


        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mSession != null) {
            mSession.stop();
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onPause();
    }

    private void setUpCaptureSequenceSession() {
        CaptureSequence sequence = createCaptureSequence();
        if (sequence == null) {
            return;
        }
        totalNumCaptures = sequence.numberCapturesRemaining();
        updateCaptureTextView();
        mSession = new CaptureSequenceSession(sequence, this, mGPSFragment);
        // mSession.addListener(this);


    }

    private Long startTimeMills;

    private CaptureSequence createCaptureSequence() {
        startTimeMills = getTestTimeFromSettings() - LEAD_TIME_SECONDS * 1000;

        boolean shouldSaveRaw = false;
        boolean shouldSaveJpeg = true;
        Long spacing = SPACING_SECONDS * 1000L;
        Long duration = SESSION_LENGTH_SECONDS * 1000;

        CaptureSequence.IntervalProperties properties = new CaptureSequence.IntervalProperties(
                SENSOR_SENSITIVITY,
                EXPOSURE_TIME,
                FOCUS_DISTANCE,
                spacing,
                shouldSaveRaw,
                shouldSaveJpeg
        );

        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(properties, startTimeMills, duration);
        return new CaptureSequence(interval);
    }

    private Long getTestTimeFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int year = prefs.getInt(getString(R.string.test_time_year), -1);
        int month = prefs.getInt(getString(R.string.test_time_month), -1);
        int dayOfMonth = prefs.getInt(getString(R.string.test_time_day_of_month), -1);
        int hours = prefs.getInt(this.getString(R.string.test_time_hour), -1);
        int minutes = prefs.getInt(getString(R.string.test_time_minute), -1);
        if (hours == -1
                || minutes == -1
                || year == -1
                || month == -1
                || dayOfMonth == -1) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

    private long getCurrentTimeMills() {
        return Calendar.getInstance().getTimeInMillis();
    }


    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        cameraFragment.takePhotoWithSettings(settings);

    }

    @Override
    public void startRecordingVideo(CaptureSequence.CaptureSettings settings) {

    }

    @Override
    public void stopRecordingVideo() {

    }


    @Override
    public void onImageCapturedInitiated() {
        numCaptures += 1;
        updateCaptureTextView();
    }


    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }
        progressTextView.setText(getString(R.string.images_captured) + ": " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }

    @Override
    public void onTick() {
        if (countdownFragment != null) {
            Long millsRemaining = targetTimeMills - Calendar.getInstance().getTimeInMillis();
            countdownFragment.setTimeRemainingMillis(millsRemaining);
            countdownFragment.onTick();
        }
        if (mSession != null) {
            mSession.onTick();
        }

    }

    private String countdownString() {
        Long millsRemaining = targetTimeMills - getCurrentTimeMills();
        String hours = DateUtil.countdownHoursString(millsRemaining);
        String minutes = DateUtil.countdownMinutesString(millsRemaining);
        String seconds = DateUtil.countdownSecondsString(millsRemaining);
        return hours + ":" + minutes + ":" + seconds;
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {
        Log.i("MoonTestCaptureActivity", "session finished");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        testTimeTextView.setVisibility(View.GONE);
        finishButton.setVisibility(View.VISIBLE);

        String message = String.format(getString(R.string.test_congrats_message), numCaptures);
        progressTextView.setText(message);
    }
}
