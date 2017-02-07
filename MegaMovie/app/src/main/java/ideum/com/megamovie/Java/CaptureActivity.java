
package ideum.com.megamovie.Java;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import ideum.com.megamovie.R;

public class CaptureActivity extends AppCompatActivity
        implements CameraFragment.CaptureListener {

    private final static String TAG = "CaptureActivity";
    private int REQUEST_LOCATION_PERMISSIONS = 0;
    private GPSFragment mGPSFragment;
    private CameraFragment mCameraFragment;
    private TextView captureTextView;
    private Integer totalCaptures;

    @Override
    public void onCapture() {
        updateCaptureTextView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        captureTextView = (TextView) findViewById(R.id.capture_text);



        /* Add Gps */
        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();

        /* Add Camera Fragment */
        mCameraFragment = new CameraFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mCameraFragment).commit();
        mCameraFragment.setLocationProvider(mGPSFragment);
        mCameraFragment.addCaptureListener(this);


        /* Set up capture sequence session */
        Resources res = getResources();
        ConfigParser parser = new ConfigParser(res.getXml(R.xml.config));
        try {
            EclipseTimeCalculator calculator = new EclipseTimeCalculator(getApplicationContext());
            EclipseCaptureSequenceBuilder builder = new EclipseCaptureSequenceBuilder(new LatLng(0, 0), parser, calculator);
            CaptureSequence sequence = builder.buildSequence();
            CaptureSequenceSession session = new CaptureSequenceSession(mCameraFragment, sequence, mGPSFragment);
            session.startSession();

            totalCaptures = sequence.getTimedRequests().size();
            updateCaptureTextView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCaptureTextView() {
        if (captureTextView == null) {
            return;
        }
        captureTextView.setText("Images Captured: " + String.valueOf(mCameraFragment.mRequestCounter) + "/" + String.valueOf(totalCaptures));
    }

    public void loadCalibrationActivity(View view) {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

    public void loadResultsActivity(View view) {
        startActivity(new Intent(this, ResultsActivity.class));
    }
}
