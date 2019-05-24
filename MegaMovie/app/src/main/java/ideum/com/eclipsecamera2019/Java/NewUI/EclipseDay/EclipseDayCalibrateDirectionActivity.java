package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ideum.com.eclipsecamera2019.Java.CameraControl.CameraPreviewAndCaptureFragment;
import ideum.com.eclipsecamera2019.Java.OrientationController.CalibrateDirectionFragment;
import ideum.com.eclipsecamera2019.Java.provider.ephemeris.Planet;
import ideum.com.eclipsecamera2019.R;

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

    @Override
    protected void onResume() {
        super.onResume();
        calibrateDirectionFragment.setShouldUseCurrentTime(true);
    }

    private void onNextButtonPressed() {
        calibrateToTarget(null);

        loadPointingActivity();
    }

    public void calibrateToTarget(View view) {
        calibrateDirectionFragment.calibrateModelToTarget();
    }




    public void loadPointingActivity() {
        Intent intent = new Intent(this,EclipseDayPointingActivity.class);
        startActivity(intent);
    }


}
