package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.eclipsecamera2019.Java.Application.Config;
import ideum.com.eclipsecamera2019.Java.Application.MyApplication;
import ideum.com.eclipsecamera2019.Java.Application.UploadActivity;
import ideum.com.eclipsecamera2019.Java.CameraControl.CameraHardwareCheckFragment;
import ideum.com.eclipsecamera2019.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequence;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceBuilder;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceBuilderDummy;
import ideum.com.eclipsecamera2019.Java.CameraControl.CaptureSequenceSession;
import ideum.com.eclipsecamera2019.Java.CameraControl.ICameraCaptureListener;
import ideum.com.eclipsecamera2019.Java.CameraControl.IVideoAndStillCamera;
import ideum.com.eclipsecamera2019.Java.CameraControl.VideoFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimeProviderOffset;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.eclipsecamera2019.Java.NewUI.EclipseInfoFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c2;
import static ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimes.Phase.c3;

public class EclipseDayCaptureActivity extends AppCompatActivity
        implements MyTimer.MyTimerListener,
        CaptureSequenceSession.CaptureSessionListener,
        ICameraCaptureListener,
        CaptureSequenceSession.CaptureSessionCompletionListerner,
        EclipseTimeProvider.Listener,
        DialogInterface.OnClickListener {

    private static final String TAG = "CaptureActivity";

    private MyTimer mTimer;
    private EclipseTimeProvider eclipseTimeProvider;
    private SmallCountdownFragment countdownFragment;

    private IVideoAndStillCamera cameraFragment;
    private CaptureSequenceSession mSession;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;

    private TextView progressTextView;
    private TextView startTimeTextView;

    private TextView recordingTextView;

    // private View progressBarView;

    Button uploadButton;
    Button finishedButton;

    Boolean audioAlertGiven = false;
    private Long startTime;
    private boolean inPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_capture);
        /**
         * Keep activity in portrait mode
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressTextView = findViewById(R.id.capture_progress_text_view);
        startTimeTextView = findViewById(R.id.start_time_text_view);
        recordingTextView = findViewById(R.id.recording_status);
        //   progressBarView = findViewById(R.id.progressBarView);
        eclipseTimeProvider = Config.USE_DUMMY_TIME_C2 ? new EclipseTimeProviderOffset() : new EclipseTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, eclipseTimeProvider).commit();

        eclipseTimeProvider.addListener(this);
        inPath = checkIfInPath();

        countdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
        cameraFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.camera_fragment);
        cameraFragment.addCaptureListener(this);
        cameraFragment.setTimeProvider(eclipseTimeProvider);
        cameraFragment.setDirectoryName(getDirectoryNameFromPreferences());
        cameraFragment.setLocationProvider(eclipseTimeProvider);

        uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUploadActivity();
            }
        });
        uploadButton.setVisibility(View.INVISIBLE);

        finishedButton = findViewById(R.id.finish_button);
        finishedButton.setVisibility(View.INVISIBLE);
        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity();
            }
        });

        recordingTextView.setText(R.string.not_recording);
        recordingTextView.setTextColor(getResources().getColor(R.color.intro_text_color_1));
    }


    @Override
    public void onEclipseTimesUpdated(EnumMap<EclipseTimes.Phase, Long> contactTimes) {
        if (timeRemaining() != null && timeRemaining() < Config.GPS_UPDATE_CUTOFF_TIME) {
            if (mSession != null) {
                return;
            }
        }
        Long c2Time = contactTimes.get(c2);
        Long c3Time = contactTimes.get(c3);
        startTime = c2Time;

        startTimeTextView.setText(getString(R.string.start_of_totality) + getStartTimeString(startTime));
        setUpCaptureSequenceSession(c2Time, c3Time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!inPath) {
            showNotInPathDialog();

        }
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.startTicking();
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        if (mSession != null) {
            mSession.stop();
            mSession = null;
        }
        super.onPause();
    }

    @Override
    public void onTick() {
        if (startTime == null) {
            return;
        }

        if (mSession != null) {
            mSession.onTick();
        }
        if (startTime != null) {
            if (countdownFragment.isAdded()) {
                Long current = eclipseTimeProvider.getTimeInMillisSinceEpoch();
                Long delta = startTime - current;
                countdownFragment.setTimeRemainingMillis(delta);
                countdownFragment.onTick();
            }


            Long timeRemaining = startTime - eclipseTimeProvider.getTimeInMillisSinceEpoch();
            if (timeRemaining <= Config.AUDIO_ALERT_TIME && !audioAlertGiven) {
                giveAudioAlert();
                audioAlertGiven = true;
            }
        }
    }

    private Long timeRemaining() {
        if (startTime == null) {
            return null;
        }
        return startTime - eclipseTimeProvider.getTimeInMillisSinceEpoch();
    }

    private void setUpCaptureSequenceSession(long c2Time, long c3Time) {

        CaptureSequence sequence = createCaptureSequence(c2Time, c3Time);
        if (sequence == null) {
            return;
        }
        if (mSession != null) {
            mSession.stop();
        }
        mSession = new CaptureSequenceSession(sequence, this, eclipseTimeProvider);

        totalNumCaptures = sequence.numberCapturesRemaining();
        updateCaptureTextView();

        if (eclipseTimeProvider.inPath()) {
            mSession.addListener(this);
            mSession.start();
        }
    }

    private CaptureSequence createCaptureSequence(long c2Time, long c3Time) {
        float magnification = (float) getLensMagnificationFromPreferences();
        if(!CameraHardwareCheckFragment.isCameraSupported() || isTotalityDurationTooShort(c2Time, c3Time)){
            return CaptureSequenceBuilder.makeSimpleVideoSequence(c2Time, c3Time);
        }
        return Config.ECLIPSE_DAY_SHOULD_USE_DUMMY_SEQUENCE ? CaptureSequenceBuilderDummy.makeSequence(c2Time) :
                CaptureSequenceBuilder.makeVideoAndImageSequence(c2Time, c3Time, magnification);
    }

    private int getLensMagnificationFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int storedValue = preferences.getInt(getString(R.string.lens_magnification_pref_key), 1);
        return Math.max(storedValue, 1);
    }

    private String getDirectoryNameFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(getString(R.string.megamovie_directory_name), "Megamovie_Images");
    }

    private boolean isTotalityDurationTooShort(long c2Time, long c3Time){
        long c2EndTime = (c2Time - Config.VIDEO_LEAD_TIME) + Config.VIDEO_DURATION;
        long c3StartTime = c3Time - Config.VIDEO_LEAD_TIME;

        long duration = c3StartTime - c2EndTime;
        return duration < Config.MIN_TOTALITY_LENGTH;
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
        builder.setMessage(getString(R.string.not_in_path_dialog))
                .setPositiveButton(getString(R.string.got_it), this)
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getStartTimeString(Long startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss a", Locale.US);

        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(calendar.getTime());
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
        Float duration = ((float) settings.exposureTime) / 1000000;
        String type = "jpg";
        if (settings.shouldSaveRaw) {
            type = "raw";
        }

        Log.i("exposure", type + ": " + String.valueOf(duration));

        cameraFragment.takePhotoWithSettings(settings);
    }

    @Override
    public void startRecordingVideo(CaptureSequence.CaptureSettings settings) {
        VideoFragment vidFrag = (VideoFragment) cameraFragment;
        if(vidFrag != null){
            vidFrag.tryToStartRecording();
        }

        recordingTextView.setText(R.string.recording);
        recordingTextView.setTextColor(getResources().getColor(R.color.green_text_color));
    }

    @Override
    public void stopRecordingVideo() {
        VideoFragment vidFrag = (VideoFragment) cameraFragment;
        if(vidFrag != null){
            vidFrag.tryToStopRecording();
        }

        recordingTextView.setText(R.string.not_recording);
        recordingTextView.setTextColor(getResources().getColor(R.color.intro_text_color_1));
        numCaptures += 1;
    }

    @Override
    public void onImageCapturedInitiated() {
        numCaptures += 1;
        updateCaptureTextView();
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {

        finishedButton.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        mTimer.cancel();
        progressTextView.setText(String.format(getString(R.string.eclipse_congrats_message), numCaptures));
        startTimeTextView.setVisibility(View.GONE);
    }

    private boolean checkIfInPath() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(getString(R.string.in_path_key), false);
    }

    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }
        progressTextView.setText(getString(R.string.images_captured) + ": " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        finish();
    }
}
