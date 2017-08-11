package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Queue;

import ideum.com.megamovie.Java.Application.UploadActivity;
import ideum.com.megamovie.Java.CameraControl.CameraFragment;
import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.CameraControl.CaptureSequence;
import ideum.com.megamovie.Java.CameraControl.CaptureSequenceSession;
import ideum.com.megamovie.Java.CameraControl.ManualCamera;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.megamovie.R;

public class EclipseDayCaptureActivity extends AppCompatActivity
implements MyTimer.MyTimerListener,
        CaptureSequenceSession.CameraController,
        CameraFragment.CaptureListener,
        CaptureSequenceSession.CaptureSessionCompletionListerner{

    private static final String TAG = "CaptureActivity";

    private static final Long BEADS_EXPOSURE_TIME = 5000000L;
    private static final Long TOTALITY_EXPOSURE_TIME = 5000000L;

    private static final Double[] EXPOSURE_FRACTIONS = {1.0/16.0,1.0/4.0,1.0,4.0,16.0};
    private static final Long BEADS_LEAD_TIME = 1000L;
    private static final Long BEADS_DURATION = 10000L;
    private static final Long BEADS_SPACING = 200L;
    private static final Long MARGIN = 1000L;
    private static final Long minRAWMargin = 1000l;


    //estimated max size of single jpeg in megabytes
    private static final float JPEG_SIZE = 0.3f;
    //estimated max size of single dng in megabytes
    private static final float RAW_SIZE = 25.0f;
    // max amount of data we're allowed to save in megabytes
    private static final float DATA_BUDGET = 1000f;

    private MyTimer mTimer;
    private EclipseTimeProvider startTimeProvider;
    private ManualCamera cameraFragment;
    private CaptureSequenceSession mSession;
    private Long targetTimeMills;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;

    private TextView progressTextView;

    private SmallCountdownFragment countdownFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_capture);

        progressTextView = (TextView) findViewById(R.id.capture_progress_text_view);

        startTimeProvider = new EclipseTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, startTimeProvider).commit();

        targetTimeMills = Calendar.getInstance().getTimeInMillis() + 15000;

        countdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        cameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);
        cameraFragment.addCaptureListener(this);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd HH:mm a");

        cameraFragment.setDirectoryName("Eclipse Day Practice " + dateFormatter.format(new Date(Calendar.getInstance().getTimeInMillis())));


        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUploadActivity();
            }
        });

    }

    private Long getC2Time() {
        return targetTimeMills;
    }

    private Long getC3Time() {
        return targetTimeMills + 120 * 1000;
    }

    private CaptureSequence createCaptureSequence() {

        long c2Time = getC2Time();//    startTimeProvider.getPhaseTimeMills(EclipseTimingMap.Event.CONTACT2);
        long c3Time = getC3Time();//    startTimeProvider.getPhaseTimeMills(EclipseTimingMap.Event.CONTACT3);
        float magnification = 1.0f;
        return makeSequence(c2Time,c3Time,magnification);

    }

    private CaptureSequence makeSequence(long c2Time,long c3Time,float magnification) {

        int sensitivity = 60;
        float focusDistance = 0f;

        long c2BaseExposureTime = (long)( BEADS_EXPOSURE_TIME/(magnification * magnification));
        boolean c2ShouldSaveRaw = false;
        boolean c2ShouldSaveJpeg = true;

        long c2StartTime = c2Time - BEADS_LEAD_TIME;
        long c2EndTime = c2StartTime + BEADS_DURATION;
        long c2Spacing = BEADS_SPACING;

        CaptureSequence.CaptureSettings c2BaseSettings = new CaptureSequence.CaptureSettings(c2BaseExposureTime,sensitivity,focusDistance,c2ShouldSaveRaw,c2ShouldSaveJpeg);
        CaptureSequence.SteppedInterval c2Interval = new CaptureSequence.SteppedInterval(c2BaseSettings,EXPOSURE_FRACTIONS,c2StartTime,c2EndTime,c2Spacing);



        long c3BaseExposureTime = (long)( BEADS_EXPOSURE_TIME/(magnification * magnification));
        boolean c3ShouldSaveRaw = false;
        boolean c3ShouldSaveJpeg = true;

        long c3StartTime = c3Time - BEADS_LEAD_TIME + MARGIN;
        long c3EndTime = c3StartTime + BEADS_DURATION;
        long c3Spacing = BEADS_SPACING;



        long totalityBaseExposureTime = (long)( TOTALITY_EXPOSURE_TIME/(magnification * magnification));
        boolean totalityShouldSaveRaw = true;
        boolean totalityShouldSaveJpeg = false;

        long totalityStartTime = c2EndTime + MARGIN;
        long totalityEndTime = c3Time - BEADS_LEAD_TIME;



        long totalitySpacing = getTotalitySpacing(totalityEndTime - totalityStartTime);

        CaptureSequence.CaptureSettings totalityBaseSettings = new CaptureSequence.CaptureSettings(totalityBaseExposureTime,sensitivity,focusDistance,totalityShouldSaveRaw,totalityShouldSaveJpeg);
        CaptureSequence.SteppedInterval totalityInterval = new CaptureSequence.SteppedInterval(totalityBaseSettings,EXPOSURE_FRACTIONS,totalityStartTime,totalityEndTime,totalitySpacing);

        CaptureSequence.CaptureSettings c3BaseSettings = new CaptureSequence.CaptureSettings(c3BaseExposureTime,sensitivity,focusDistance,c3ShouldSaveRaw,c3ShouldSaveJpeg);
        CaptureSequence.SteppedInterval c3Interval = new CaptureSequence.SteppedInterval(c3BaseSettings,EXPOSURE_FRACTIONS,c3StartTime,c3EndTime,c3Spacing);

        CaptureSequence.SteppedInterval[] intervals = {c2Interval,totalityInterval,c3Interval};
        Log.i("CAPTURES","jpeg :" + String.valueOf(c2Interval.getRequests().size()));
        Log.i("CAPTURES","raw :" + String.valueOf(totalityInterval.getRequests().size()));
        Log.i("CAPTURES","jpeg :" + String.valueOf(c3Interval.getRequests().size()));


        return new CaptureSequence(intervals);
    }

    private long getTotalitySpacing(long duration) {
        int numTotalityCaptures = (int)(totalityDataBudget()/RAW_SIZE);
        long idealSpacing = (long)(duration/(float)numTotalityCaptures);
        return Math.max(idealSpacing,minRAWMargin);
    }

    private float totalityDataBudget() {
        float beadsDataUsage = 2 * JPEG_SIZE * BEADS_DURATION /(float)BEADS_SPACING;
        return DATA_BUDGET - beadsDataUsage;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.addListener(countdownFragment);
        mTimer.startTicking();

    }

    private void setUpCaptureSequenceSession() {
        CaptureSequence sequence = createCaptureSequence();
        if (sequence == null) {
            return;
        }
        mSession = new CaptureSequenceSession(sequence, this);
        totalNumCaptures = sequence.numberCapturesRemaining();
        updateCaptureTextView();

        mSession.addListener(this);
        mSession.start();

    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        if (mSession != null) {
            mSession.stop();
        }
        super.onPause();
    }

    public void goToUploadActivity() {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTick() {
        Long millsRemaining = getC2Time();
//        if (millsRemaining != null && mSession == null) {
//            setUpCaptureSequenceSession();
//        }
        countdownFragment.setTargetTimeMills(millsRemaining);
        countdownFragment.onTick();
    }

    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        cameraFragment.takePhotoWithSettings(settings);
    }

    @Override
    public void onCapture() {
        numCaptures += 1;
        updateCaptureTextView();
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {
        Toast.makeText(this,"sessions completed",Toast.LENGTH_SHORT);
    }

    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }
        progressTextView.setText("Images Captured: " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }
}
