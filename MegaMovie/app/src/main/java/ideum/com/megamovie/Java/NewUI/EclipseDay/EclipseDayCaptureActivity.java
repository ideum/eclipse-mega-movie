package ideum.com.megamovie.Java.NewUI.EclipseDay;

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
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Locale;
import java.util.TimeZone;

import ideum.com.megamovie.Java.Application.Config;
import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.Java.Application.UploadActivity;
import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.CameraControl.CaptureSequence;
import ideum.com.megamovie.Java.CameraControl.CaptureSequenceBuilder;
import ideum.com.megamovie.Java.CameraControl.CaptureSequenceBuilderDummy;
import ideum.com.megamovie.Java.CameraControl.CaptureSequenceSession;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeProviderOffset;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes;
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
        CameraPreviewAndCaptureFragment.CameraCaptureListener,
        CaptureSequenceSession.CaptureSessionCompletionListerner,
        EclipseTimeProvider.Listener {

    private static final String TAG = "CaptureActivity";

    private MyTimer mTimer;
    private EclipseTimeProvider eclipseTimeProvider;
    private SmallCountdownFragment countdownFragment;

    private CameraPreviewAndCaptureFragment cameraFragment;
    private CaptureSequenceSession mSession;

    private int numCaptures = 0;
    private int totalNumCaptures = 0;

    private TextView progressTextView;
    private TextView startTimeTextView;

    Button uploadButton;
    Button finishedButton;

    Boolean audioAlertGiven = false;
    private Long startTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_capture);
        /**
         * Keep activity in portrait mode
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressTextView = (TextView) findViewById(R.id.capture_progress_text_view);
        startTimeTextView = (TextView) findViewById(R.id.start_time_text_view);

        eclipseTimeProvider = Config.USE_DUMMY_TIME_C2 ? new EclipseTimeProviderOffset() : new EclipseTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, eclipseTimeProvider).commit();

        eclipseTimeProvider.addListener(this);

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

            countdownFragment.setTargetTimeMills(startTime);
            countdownFragment.onTick();

            Long timeRemaining = startTime - Calendar.getInstance().getTimeInMillis();
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
        return startTime - Calendar.getInstance().getTimeInMillis();
    }

    private void setUpCaptureSequenceSession(long c2Time, long c3Time) {

        CaptureSequence sequence = createCaptureSequence(c2Time, c3Time);
        if (sequence == null) {
            return;
        }
        if (mSession != null) {
            mSession.stop();
        }
        mSession = new CaptureSequenceSession(sequence, this);
        totalNumCaptures = sequence.numberCapturesRemaining();
        updateCaptureTextView();

        if (eclipseTimeProvider.inPath()) {
            mSession.addListener(this);
            mSession.start();
        }
    }

    private CaptureSequence createCaptureSequence(long c2Time, long c3Time) {

        if (Config.ECLIPSE_DAY_SHOULD_USE_DUMMY_SEQUENCE) {
            return dummySequence();
        }
        float magnification = (float) getLensMagnificationFromPreferences();
        return Config.ECLIPSE_DAY_SHOULD_USE_DUMMY_SEQUENCE ? CaptureSequenceBuilderDummy.makeSequence(c2Time) :
                CaptureSequenceBuilder.makeSequence(c2Time, c3Time, magnification);
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
                .setPositiveButton(getString(R.string.got_it), null)
                .setCancelable(true);

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
    public void onCapture() {
        numCaptures += 1;
        updateCaptureTextView();
    }

    @Override
    public void onSessionCompleted(CaptureSequenceSession session) {
        //   Log.i("CAPTURE", "session completed");
        //  Toast.makeText(this, "SessionCompleted!", Toast.LENGTH_LONG).show();
        finishedButton.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        mTimer.cancel();
        progressTextView.setText(String.format(getString(R.string.eclipse_congrats_message), numCaptures));
        startTimeTextView.setVisibility(View.GONE);


    }


    private void updateCaptureTextView() {
        if (progressTextView == null) {
            return;
        }
        progressTextView.setText(getString(R.string.images_captured) + ": " + String.valueOf(numCaptures) + "/" + String.valueOf(totalNumCaptures));
    }

    private CaptureSequence dummySequence() {
        Long duration = 5000000L;
        int sensitivity = 60;
        float focusDistance = 0f;
        boolean shouldUseJpeg = true;
        boolean shouldUseRaw = false;

        CaptureSequence.CaptureSettings settings = new CaptureSequence.CaptureSettings(duration, sensitivity, focusDistance, shouldUseRaw, shouldUseJpeg);

        Long startTime = Calendar.getInstance().getTimeInMillis() + 6000;
        Long spacing = 500L;
        CaptureSequence.CaptureInterval interval = new CaptureSequence.CaptureInterval(settings, spacing, startTime, 10000L);


        return new CaptureSequence(interval);
    }

}
