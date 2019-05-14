package ideum.com.eclipsecamera2019.Java.NewUI.MoonTest;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ideum.com.eclipsecamera2019.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequence;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceSession;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.GPSFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.R;

public class MoonTestHandHeldCaptureActivity extends AppCompatActivity
        implements CaptureSequenceSession.CameraController,
        CameraPreviewAndCaptureFragment.CameraCaptureListener {
    private static final int CONFIG_ID = R.xml.moon_test_config;
    private CameraPreviewAndCaptureFragment cameraFragment;
    private MyTimer mTimer;
    private CaptureSequenceSession mSession;
    private static final long SESSION_LENGTH_SECONDS = 360;
    private static final int LEAD_TIME_SECONDS = 180;
    private static final int SPACING_SECONDS = 30;

    private static final long EXPOSURE_TIME = 5000000L;
    private static int SENSOR_SENSITIVITY = 60;
    private static float FOCUS_DISTANCE = 0f;

    private TextView testTimeTextView;
    private TextView leadTimeTextView;
    private TextView durationTextView;
    //private TextView countdownTextView;
    private TextView progressTextView;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_test_hand_held_capture);

        // Set portrait mode and keep phone from sleeping
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // gps just used for image metadata in practice mode
        GPSFragment mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        cameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);

        cameraFragment.setLocationProvider(mGPSFragment);

        cameraFragment.addCaptureListener(this);

        testTimeTextView = (TextView) findViewById(R.id.test_time_text_view);
        leadTimeTextView = (TextView) findViewById(R.id.lead_time_text_view);
        //countdownTextView = (TextView) findViewById(R.id.capture_countdown_text_view);
        durationTextView = (TextView) findViewById(R.id.duration_text_view);

        progressTextView = (TextView) findViewById(R.id.capture_progress_text_view);


        Long testTime = getTestTimeFromSettings();

        Date testTimeDate = new Date(testTime);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        String testTimeString = "Test time start: " + timeFormatter.format(testTimeDate);

        int method = getMethodNumFromSettings();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yy_MM_dd_HH_mm");


        cameraFragment.setDirectoryName("Megamovie_test_mthd" + String.valueOf(method) + "_" + dateFormatter.format(testTimeDate));

        testTimeTextView.setText(testTimeString);

        int leadTimeMinutes = LEAD_TIME_SECONDS / 60;
        int leadTimeSeconds = LEAD_TIME_SECONDS - 60 * leadTimeMinutes;
        String leadTimeMinutesString = String.format("%02d",leadTimeMinutes);
        String  leadTimeSecondsString = String.format("%02d",leadTimeSeconds);
        String leadTimeString = "Lead time: " + leadTimeMinutesString + ":" + leadTimeSecondsString;
        leadTimeTextView.setText(leadTimeString);

        int durationMinutes = (int)SESSION_LENGTH_SECONDS / 60;
        int durationSeconds = (int)SESSION_LENGTH_SECONDS - 60 * durationMinutes;
        String durationMinutesString = String.format("%02d",durationMinutes);
        String  durationSecondsString = String.format("%02d",durationSeconds);
        String durationString = "Duration: " + durationMinutesString + ":" + durationSecondsString;
        durationTextView.setText(durationString);

    }

    private int getMethodNumFromSettings() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        return pref.getInt(getString(R.string.calibration_method),-1);
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

    private void setUpCaptureSequenceSession() {
        CaptureSequence sequence = createCaptureSequence();
        if (sequence == null) {
            return;
        }
        totalNumCaptures = sequence.numberCapturesRemaining();
        updateCaptureTextView();
        mSession = new CaptureSequenceSession(sequence, this);

        mTimer = new MyTimer();
        mTimer.addListener(mSession);
        mTimer.startTicking();
    }
    private CaptureSequence createCaptureSequence() {
        Long startTimeMills = getTestTimeFromSettings() - LEAD_TIME_SECONDS * 1000;

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
                shouldSaveJpeg,
                false);

        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(properties,startTimeMills,duration);
        return new CaptureSequence(interval);
    }

    private Long getTestTimeFromSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int year = prefs.getInt(getString(R.string.test_time_year),-1);
        int month = prefs.getInt(getString(R.string.test_time_month),-1);
        int dayOfMonth = prefs.getInt(getString(R.string.test_time_day_of_month),-1);
        int hours = prefs.getInt(this.getString(R.string.test_time_hour),-1);
        int minutes = prefs.getInt(getString(R.string.test_time_minute),-1);
        if (hours == -1
                || minutes == -1
                || year == -1
                || month == -1
                || dayOfMonth == -1){
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY,hours);
        c.set(Calendar.MINUTE,minutes);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);

        return c.getTimeInMillis();
    }

    private long getCurrentTimeMills() {
        return Calendar.getInstance().getTimeInMillis();
    }



    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        cameraFragment.takePhotoWithSettings(settings);
//        cameraFragment.setCameraSettings(settings);
//        cameraFragment.captureStillImage();
    }

    @Override
    public void onCapture() {
        numCaptures += 1;
        updateCaptureTextView();
    }


    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }
        progressTextView.setText("Images Captured: " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }

}
