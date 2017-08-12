package ideum.com.megamovie.Java.OrientationController;

import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.LocationSource;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Provider;

import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.Util.MiscUtil;
import ideum.com.megamovie.Java.Util.VectorUtil;
import ideum.com.megamovie.Java.Util.smoothers.PlainSmootherModelAdaptor;
import ideum.com.megamovie.Java.VIews.DirectionCalibrationView;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.Java.provider.ephemeris.SolarPositionCalculator;
import ideum.com.megamovie.Java.units.GeocentricCoordinates;
import ideum.com.megamovie.Java.units.LatLong;
import ideum.com.megamovie.Java.units.Matrix33;
import ideum.com.megamovie.Java.units.RaDec;
import ideum.com.megamovie.Java.units.Vector3;
import ideum.com.megamovie.R;

import static android.content.Context.SENSOR_SERVICE;
import static android.view.View.GONE;


public class CalibrateDirectionFragment extends Fragment
implements MyTimer.MyTimerListener,
        LocationSource.OnLocationChangedListener{

    final AstronomerModelImpl model = new AstronomerModelImpl(new RealMagneticDeclinationCalculator());
    private MyTimer mTimer;
    private GPSFragment mGPSFragment;
    DirectionCalibrationView calibrationView;
    public boolean shouldUseCurrentTime = true;
    private Long targetTimeMills = 0L;

    private Planet planet = Planet.Moon;

    private TextView sunRaDec;
    private TextView phoneRaDec;

    public CalibrateDirectionFragment() {
        // Required empty public constructor
    }

    public void setTarget(Planet target) {
        planet = target;
    }

    public void setTargetTimeMills(Long mills) {
        targetTimeMills = mills;
        FixedClock c = new FixedClock(mills);
        model.setClock(c);
    }

    public void setShouldUseCurrentTime(boolean value) {
        if (value) {
            model.setClock(new RealClock());
            shouldUseCurrentTime = true;
        } else {
            shouldUseCurrentTime = false;
            FixedClock c = new FixedClock(targetTimeMills);
            model.setClock(c);
        }

    }

    public static CalibrateDirectionFragment newInstance() {
        CalibrateDirectionFragment fragment = new CalibrateDirectionFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calibrate_direction, container, false);

        calibrationView = rootView.findViewById(R.id.guide_view);
        sunRaDec = rootView.findViewById(R.id.sun_ra_dec);
        phoneRaDec = rootView.findViewById(R.id.phone_ra_dec);
        sunRaDec.setVisibility(GONE);
        phoneRaDec.setVisibility(GONE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        final PlainSmootherModelAdaptor psma = new PlainSmootherModelAdaptor(model, preferences);

        Provider<PlainSmootherModelAdaptor> psmap = new Provider<PlainSmootherModelAdaptor>() {
            @Override
            public PlainSmootherModelAdaptor get() {
                return psma;
            }
        };

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        SensorOrientationController sensorOrientationController = new SensorOrientationController(psmap, sensorManager, preferences);
        sensorOrientationController.setModel(model);
        sensorOrientationController.start();


        return rootView;
    }

    public void showView(boolean shouldShow) {
        if (shouldShow) {
            calibrationView.setVisibility(View.VISIBLE);
        } else {
            calibrationView.setVisibility(GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        mGPSFragment = new GPSFragment();
        getActivity().getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.activate(this);

        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.TICK_INTERVAL = 30;
        mTimer.startTicking();
    }

    @Override
    public void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onPause();
    }


    private Date getDate() {
        if (shouldUseCurrentTime) {
            return new Date(System.currentTimeMillis());
        }
        if (targetTimeMills == null) {
            return null;
        }
        return  new Date(targetTimeMills);
    }

    private Float error() {
        Vector3 lineOfSight = getLineOfSightVector();
        Vector3 target = getTargetVector();
        if (lineOfSight == null || target == null) {
            return null;
        }

        return VectorUtil.difference(lineOfSight,target).length();
    }



    public void calibrateModelToTarget() {
        model.calibrate(getTargetGcc());

        //storeNorthInPhoneCoordinates();
        //storePhoneInLocalCoordinates();

        storeCorrectionMatrix();

    }

    private void storeCorrectionMatrix() {
        Matrix33 correctionMatrix = model.correctionMatrix;
        MiscUtil.storeMatrix33InPreferences(getContext(),getString(R.string.stored_correction_matrix_key),correctionMatrix);
    }

    private Matrix33 getStoredCorrectionMatrixFromPrefs() {
        return MiscUtil.getMatrix33FromPreferences(getContext(),getString(R.string.stored_correction_matrix_key),Matrix33.getIdMatrix());
    }

    private void storeNorthInPhoneCoordinates() {
        Vector3 storedNorthInPhoneCooredinates = model.storedNorthInPhoneCoordinates;
        MiscUtil.storeVector3InPreferences(getContext(),getString(R.string.stored_north_key),storedNorthInPhoneCooredinates);
    }

    private void storePhoneInLocalCoordinates() {
        Matrix33 storedPhoneInLocalCoordinates = model.storedPhoneInLocalCoordinates;
        MiscUtil.storeMatrix33InPreferences(getContext(),getString(R.string.stored_phone_coordinates_key),storedPhoneInLocalCoordinates);
    }

    private Vector3 getStoredNorthInPhoneCoordinatesFromPrefs() {
        return MiscUtil.getVector3FromPreferences(getContext(),getString(R.string.stored_north_key),new Vector3 (0,1,0));
    }

    private Matrix33 getStoredPhoneCoordinatesFromPrefs() {
        return MiscUtil.getMatrix33FromPreferences(getContext(),getString(R.string.stored_phone_coordinates_key),Matrix33.getIdMatrix());
    }

    public void calibrateModelFromSettings() {
       // model.storedNorthInPhoneCoordinates = getStoredNorthInPhoneCoordinatesFromPrefs();
       // model.storedPhoneInLocalCoordinates = getStoredPhoneCoordinatesFromPrefs();
        model.correctionMatrix = getStoredCorrectionMatrixFromPrefs();
        model.isCalibrated = true;
    }

    public void resetModelCalibration() {
        model.resetCalibration();
    }

    private AstronomerModel.Pointing pointing() {
//        return model.getPointing();
        return model.getCorrectedPointing();
    }

    private Vector3 getLineOfSightVector() {
        if (model == null) {
            return null;
        }

        return pointing().getLineOfSight().getVector3();

    }
    private Vector3 getPerpendicularVector() {
        if (model == null) {
            return null;
        }
        return pointing().getPerpendicular().getVector3();
    }

    public RaDec getTargetRaDec() {
        float ra = getTargetGcc().getRa();
        float dec = getTargetGcc().getDec();
        return new RaDec(ra,dec);
    }

    public RaDec getPhoneRaDec() {
        float ra = pointing().getLineOfSight().getRa();
        float dec = pointing().getLineOfSight().getDec();
        //Log.i("calibration",String.valueOf(ra));
        return new RaDec(ra,dec);
    }

    private Vector3 getTargetVector() {

        GeocentricCoordinates targetGcc = getTargetGcc();
        if (targetGcc == null) {
            return null;
        }
        return getTargetGcc().getVector3();
    }

    private GeocentricCoordinates getTargetGcc() {
        if (model == null) {
            return null;
        }
        Date date = getDate();
        if (date == null) {
            return null;
        }
        RaDec targetRaDec = null;
        switch (planet) {
            case Moon:
                targetRaDec = Planet.calculateLunarGeocentricLocation(date);
                break;
            case Sun:
                targetRaDec = SolarPositionCalculator.getSolarPosition(date);
                break;
        }
        GeocentricCoordinates targetGcc = new GeocentricCoordinates(1, 0, 0);
        targetGcc.updateFromRaDec(targetRaDec);
        return targetGcc;
    }

    private DirectionCalibrationView.ScreenVector getErrorVector() {
        Vector3 phoneOutwards = getLineOfSightVector();
        if (phoneOutwards == null) {
            return null;
        }
        Vector3 phoneY = VectorUtil.negate(getPerpendicularVector());
        Vector3 phoneX = VectorUtil.crossProduct(phoneOutwards,phoneY);
        if (getTargetVector() == null) {
            return null;
        }
        Vector3 errorVector3 = VectorUtil.difference(getTargetVector(),phoneOutwards);

        float x = -VectorUtil.dotProduct(errorVector3,phoneX)/2.0f;
        float y = VectorUtil.dotProduct(errorVector3,phoneY)/2.0f;
        float l = (float) Math.sqrt(x*x + y*y);
        x = x/(float)Math.sqrt(l);
        y = y/(float)Math.sqrt(l);

        return new DirectionCalibrationView.ScreenVector(x,y);
    }

    private void updateCalibrationView() {

        float arrowX = getErrorVector().x;
        float arrowY = getErrorVector().y;
        float error = 0.5f * (float) Math.sqrt(arrowX * arrowX + arrowY * arrowY);


        calibrationView.setArrowX(arrowX);
        calibrationView.setArrowY(arrowY);
        calibrationView.setCircleRadius(1.0f/(error + 0.1f)/10.0f);
    }
    @Override
    public void onTick() {
    Date date = getDate();


        updateUI();
        updateCalibrationView();
    }

    private void updateUI() {

        phoneRaDec.setText("phone:\n" + getPhoneRaDec().toString());

        Date date = getDate();
        if (date == null) {
            return;
        }


//        sunRaDec.setText(((AstronomerModelImpl)model).inverseMatrix());

        sunRaDec.setText("target:\n" + getTargetRaDec().toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        LatLong latLng = new LatLong(location.getLatitude(), location.getLongitude());
        if (model != null) {
            model.setLocation(latLng);
        }
    }
}
