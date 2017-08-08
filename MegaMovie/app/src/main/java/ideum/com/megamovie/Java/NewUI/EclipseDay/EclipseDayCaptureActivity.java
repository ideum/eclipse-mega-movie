package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ideum.com.megamovie.Java.Application.UploadActivity;
import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.LocationAndTiming.SmallCountdownFragment;
import ideum.com.megamovie.Java.LocationAndTiming.TotalityStartTimeProvider;
import ideum.com.megamovie.R;

public class EclipseDayCaptureActivity extends AppCompatActivity
implements MyTimer.MyTimerListener{

    private static final String TAG = "CaptureActivity";
    private MyTimer mTimer;
    private TotalityStartTimeProvider startTimeProvider;

    private SmallCountdownFragment countdownFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_capture);

        startTimeProvider = new TotalityStartTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, startTimeProvider).commit();

        countdownFragment = (SmallCountdownFragment) getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);


        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUploadActivity();
            }
        });
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
        super.onPause();
    }

    public void goToUploadActivity() {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTick() {
        Log.i(TAG, "tick");
        Long millsRemaining = startTimeProvider.getStartOfTotalityMills();
        countdownFragment.setTargetTimeMills(millsRemaining);
        countdownFragment.onTick();
    }
}
