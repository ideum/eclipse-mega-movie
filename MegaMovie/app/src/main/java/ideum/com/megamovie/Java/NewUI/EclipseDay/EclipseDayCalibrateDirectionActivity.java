package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.megamovie.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.R;

public class EclipseDayCalibrateDirectionActivity extends AppCompatActivity{

    private CalibrateDirectionFragment calibrateDirectionFragment;



    private CameraPreviewAndCaptureFragment mCameraFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_calibrate_direction);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        calibrateDirectionFragment = (CalibrateDirectionFragment) getSupportFragmentManager().findFragmentById(R.id.direction_calibration_fragment);
        calibrateDirectionFragment.shouldUseCurrentTime = true;
        calibrateDirectionFragment.setTarget(Planet.Sun);

        calibrateDirectionFragment.resetModelCalibration();
        calibrateDirectionFragment.showView(false);

        mCameraFragment = (CameraPreviewAndCaptureFragment) getFragmentManager().findFragmentById(R.id.camera_preview_fragment);

        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonPressed();
            }
        });

        //showSolarFilterAlert();
    }

    private void onNextButtonPressed() {
        calibrateToTarget(null);

        loadPointingActivity();
    }

    public void calibrateToTarget(View view) {
        calibrateDirectionFragment.calibrateModelToTarget();
    }

    public void resetCalibration(View view) {
        calibrateDirectionFragment.resetModelCalibration();
    }

    private void showSolarFilterAlert() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure your lens is covered with a solar filter.")
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void loadPointingActivity() {
        Intent intent = new Intent(this,EclipseDayPointingActivity.class);
        startActivity(intent);
    }


}
