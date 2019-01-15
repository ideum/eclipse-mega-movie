package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;

import ideum.com.megamovie.Java.Application.Config;
import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.Java.Application.UploadActivity;
import ideum.com.megamovie.Java.Application.UploadTestActivity;
import ideum.com.megamovie.Java.CameraControl.CameraFragment;
import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.CameraControl.CaptureSequence;
import ideum.com.megamovie.Java.CameraControl.CaptureSequenceSession;
import ideum.com.megamovie.Java.CameraControl.ManualCamera;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.megamovie.Java.NewUI.EclipseInfoFragment;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes.Phase.c3;

public class EclipseDayCaptureActivity extends AppCompatActivity
implements MyTimer.MyTimerListener,
        CaptureSequenceSession.CameraController,
        CameraFragment.CaptureListener,
        CaptureSequenceSession.CaptureSessionCompletionListerner{

    private static final String TAG = "CaptureActivity";

    private MyTimer mTimer;
    private EclipseTimeProvider eclipseTimeProvider;
    private SmallCountdownFragment countdownFragment;

    private ManualCamera cameraFragment;
    private CaptureSequenceSession mSession;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;

    private TextView progressTextView;
    private TextView startTimeTextView;

    Button uploadButton;
    Button finishedButton;

    Boolean audioAlertGiven = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_capture);

        progressTextView = (TextView) findViewById(R.id.capture_progress_text_view);
        startTimeTextView = (TextView) findViewById(R.id.start_time_text_view);

        eclipseTimeProvider = new EclipseTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, eclipseTimeProvider).commit();

        if (!eclipseTimeProvider.inPath()) {
            progressTextView.setVisibility(View.GONE);
        }

        countdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        cameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);
        cameraFragment.addCaptureListener(this);

        cameraFragment.setDirectoryName(getDirectoryNameFromPreferences());
        cameraFragment.setLocationProvider(eclipseTimeProvider);

        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUploadActivity();
            }
        });
        uploadButton.setVisibility(View.INVISIBLE);

        finishedButton = (Button) findViewById(R.id.finish_button);
        finishedButton.setVisibility(View.INVISIBLE);
        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.addListener(countdownFragment);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        if (mSession != null) {
            mSession.stop();
        }
        super.onPause();
    }


    @Override
    public void onTick() {
        Long startTime = getC2Time();
        if (startTime!= null) {
            if (mSession == null) {
                if(!eclipseTimeProvider.inPath()) {
                    showNotInPathDialog();
                }
                setUpCaptureSequenceSession();
                startTimeTextView.setText("Start of Totality: " + getStartTimeString(startTime));
            }
            countdownFragment.setTargetTimeMills(startTime);
            countdownFragment.onTick();

            Long timeRemaining = startTime - Calendar.getInstance().getTimeInMillis();
            if (timeRemaining <= 18000 && !audioAlertGiven) {
                giveAudioAlert();
                audioAlertGiven = true;
            }
        }
    }

    private void setUpCaptureSequenceSession() {
        CaptureSequence sequence = createCaptureSequence();
        if (sequence == null) {
            return;
        }
        mSession = new CaptureSequenceSession(sequence, this);
        totalNumCaptures = sequence.numberCapturesRemaining();
        updateCaptureTextView();

        if (eclipseTimeProvider.inPath()) {

            mSession.addListener(this);
            mSession.start();
        }

    }

    private CaptureSequence createCaptureSequence() {

        if (Config.ECLIPSE_DAY_SHOULD_USE_DUMMY_SEQUENCE) {
            return dummySequence();
        }

        long c2Time = getC2Time();
        long c3Time = getC3Time();
        float magnification = (float)getLensMagnificationFromPreferences();
        return makeSequence(c2Time,c3Time,magnification);

    }

    private CaptureSequence makeSequence(long c2Time,long c3Time,float magnification) {

        int sensitivity = 60;
        float focusDistance = 0f;

        long c2BaseExposureTime = (long)( Config.BEADS_EXPOSURE_TIME/(magnification * magnification));
        boolean c2ShouldSaveRaw = Config.beadsShouldCaptureRaw;
        boolean c2ShouldSaveJpeg = Config.beadsShouldCaptureJpeg;

        long c2StartTime = c2Time - Config.BEADS_LEAD_TIME;
        long c2EndTime = c2StartTime + Config.BEADS_DURATION;
        long c2Spacing = Config.BEADS_SPACING;

        CaptureSequence.CaptureSettings c2BaseSettings = new CaptureSequence.CaptureSettings(c2BaseExposureTime,sensitivity,focusDistance,c2ShouldSaveRaw,c2ShouldSaveJpeg);
        CaptureSequence.SteppedInterval c2Interval = new CaptureSequence.SteppedInterval(c2BaseSettings, Config.BEADS_FRACTIONS,c2StartTime,c2EndTime,c2Spacing);

        long c3BaseExposureTime = (long)( Config.BEADS_EXPOSURE_TIME/(magnification * magnification));
        boolean c3ShouldSaveRaw = false;
        boolean c3ShouldSaveJpeg = true;

        long c3StartTime = c3Time - Config.BEADS_LEAD_TIME;
        long c3EndTime = c3StartTime + Config.BEADS_DURATION;
        long c3Spacing = Config.BEADS_SPACING;

        long totalityBaseExposureTime =  Config.TOTALITY_EXPOSURE_TIME;

        long totalityStartTime = c2EndTime + Config.MARGIN;
        long totalityEndTime = c3StartTime - Config.MARGIN;

        long totalitySpacing = getTotalitySpacing(totalityEndTime - totalityStartTime);

        CaptureSequence.CaptureSettings totalityBaseSettings = new CaptureSequence.CaptureSettings(totalityBaseExposureTime,sensitivity,focusDistance,Config.totalityShouldCaptureRaw,Config.totalityShouldCaptureJpeg);
        CaptureSequence.SteppedInterval totalityInterval = new CaptureSequence.SteppedInterval(totalityBaseSettings, Config.TOTALITY_FRACTIONS,totalityStartTime,totalityEndTime,totalitySpacing);

        CaptureSequence.CaptureSettings c3BaseSettings = new CaptureSequence.CaptureSettings(c3BaseExposureTime,sensitivity,focusDistance,c3ShouldSaveRaw,c3ShouldSaveJpeg);
        CaptureSequence.SteppedInterval c3Interval = new CaptureSequence.SteppedInterval(c3BaseSettings, Config.BEADS_FRACTIONS,c3StartTime,c3EndTime,c3Spacing);

        CaptureSequence.SteppedInterval[] intervals = {c2Interval,totalityInterval,c3Interval};

        Queue<CaptureSequence.TimedCaptureRequest> requests = totalityInterval.getRequests();

        return new CaptureSequence(intervals);
    }



    private Long getC2Time() {
        return  eclipseTimeProvider.getPhaseTimeMills(c2);
    }

    private Long getC3Time() {
        Long c3Time = eclipseTimeProvider.getPhaseTimeMills(c3);
        Long c3MinTime = getC2Time() + 30 * 1000L;
        return Math.max(c3Time,c3MinTime);
    }

    private int getLensMagnificationFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int storedValue = preferences.getInt(getString(R.string.lens_magnification_pref_key),1);
        return Math.max(storedValue,1);
    }

    private String getDirectoryNameFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(getString(R.string.megamovie_directory_name),"Megamovie_Images");
    }



    private long getTotalitySpacing(long duration) {
        int numTotalityCaptures = (int)(totalityDataBudget()/Config.RAW_SIZE);
        long idealSpacing = (long)(duration/(float)numTotalityCaptures);
        return Math.max(idealSpacing,Config.minRAWMargin);
    }

    private float totalityDataBudget() {
        float beadsDataUsage = 2 * Config.JPEG_SIZE * Config.BEADS_DURATION /(float)Config.BEADS_SPACING;
        return Config.DATA_BUDGET - beadsDataUsage;
    }



    public void goToUploadActivity() {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    public void goToMainActivity() {
        MyApplication application = (MyApplication) getApplication();
        application.currentFragment = EclipseInfoFragment.class;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    private void showNotInPathDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You are not currently in the path of totality. The countdown and eclipse time displayed are based on the point in the path closest to your current location.")
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getStartTimeString(Long startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss a", Locale.US);

        formatter.setTimeZone(TimeZone.getDefault());
        String   timeZoneDisplayName = TimeZone.getDefault().getDisplayName(true,TimeZone.SHORT,Locale.US);


        return formatter.format(calendar.getTime());// + " " + timeZoneDisplayName;
    }


    private void giveAudioAlert() {
                try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {
        Float duration = ((float)settings.exposureTime)/1000000;
        String type = "jpg";
        if(settings.shouldSaveRaw) {
            type = "raw";
        }

        Log.i("exposure",type + ": " + String.valueOf(duration));

        cameraFragment.takePhotoWithSettings(settings);
    }

    @Override
    public void onCapture() {
        numCaptures += 1;
        updateCaptureTextView();
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {
        Log.i("CAPTURE", "session completed");
        Toast.makeText(this,"SessionCompleted!",Toast.LENGTH_LONG).show();
        finishedButton.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        mTimer.cancel();
        progressTextView.setText(String.format("Congratulations! You captured %d images of the eclipse. You can upload them now or any time later on.",numCaptures));
        startTimeTextView.setVisibility(View.GONE);


    }


    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }
        progressTextView.setText("Images Captured: " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }

    private CaptureSequence dummySequence() {
        Long duration = 5000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration,sensitivity,focusDistance,shouldUseRaw,shouldUseJpeg);

        Long startTime = Calendar.getInstance().getTimeInMillis() + 2000;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings,spacing,startTime,1500L);


        return new CaptureSequence(interval);
    }

}
