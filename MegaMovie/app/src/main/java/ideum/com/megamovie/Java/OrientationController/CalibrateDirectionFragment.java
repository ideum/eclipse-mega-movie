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
import ideum.com.megamovie.Java.Util.VectorUtil;
import ideum.com.megamovie.Java.Util.smoothers.PlainSmootherModelAdaptor;
import ideum.com.megamovie.Java.VIews.DirectionCalibrationView;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.Java.provider.ephemeris.SolarPositionCalculator;
import ideum.com.megamovie.Java.units.GeocentricCoordinates;
import ideum.com.megamovie.Java.units.LatLong;
import ideum.com.megamovie.Java.units.RaDec;
import ideum.com.megamovie.Java.units.Vector3;
import ideum.com.megamovie.R;

import static android.content.Context.SENSOR_SERVICE;


public class CalibrateDirectionFragment extends Fragment
implements MyTimer.MyTimerListener,
        LocationSource.OnLocationChangedListener{

    final AstronomerModel model = new AstronomerModelImpl(new RealMagneticDeclinationCalculator());
    private MyTimer mTimer;
    private GPSFragment mGPSFragment;
    DirectionCalibrationView calibrationView;
    private boolean shouldUseCurrentTime = false;
    private Long targetTimeMills;

    private Planet planet = Planet.Sun;

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

//        updateTargetTimeMillsFromPreferences();

        return rootView;
    }

//    private void updateTargetTimeMillsFromPreferences() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        int hour = preferences.getInt(getContext().getString(R.string.moon_test_hour),-1);
//        int minute = preferences.getInt(getContext().getString(R.string.moon_test_minute),-1);
//        if (hour == -1 || minute == -1) {
//            return;
//        }
//
//        Calendar c = Calendar.getInstance();
//        c.set(Calendar.HOUR_OF_DAY,hour);
//        c.set(Calendar.MINUTE,minute);
//        c.set(Calendar.MILLISECOND,0);
//        targetTimeMills = c.getTimeInMillis();
//    }

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
//        if (model == null) {
//            return null;
//        }
//        GeocentricCoordinates lineOfSightPointing = model.getPointing().getLineOfSight();
//        float lineOfSightX = lineOfSightPointing.x;
//        float lineOfSightY = lineOfSightPointing.y;
//        float lineOfSightZ = lineOfSightPointing.z;
//
//        RaDec targetRA = null;
//        switch (planet) {
//            case Moon:
//                targetRA = Planet.calculateLunarGeocentricLocation(model.getTime());
//                break;
//            case Sun:
//                targetRA = SolarPositionCalculator.getSolarPosition(model.getTime());
//                break;
//        }
//
//        GeocentricCoordinates targetGcc = new GeocentricCoordinates(1, 0, 0);
//        targetGcc.updateFromRaDec(targetRA);
//        float targetX = targetGcc.x;
//        float targetY = targetGcc.y;
//        float targetZ = targetGcc.z;

//        Vector3 difference = new Vector3(targetX - lineOfSightX, targetY - lineOfSightY, targetZ - lineOfSightZ);
        Vector3 lineOfSight = getLineOfSightVector();
        Vector3 target = getTargetVector();
        if (lineOfSight == null || target == null) {
            return null;
        }

        return VectorUtil.difference(lineOfSight,target).length();
    }

    private Vector3 getLineOfSightVector() {
        if (model == null) {
            return null;
        }

        return model.getPointing().getLineOfSight().getVector3();

    }
    private Vector3 getPerpendicularVector() {
        if (model == null) {
            return null;
        }
        return model.getPointing().getPerpendicular().getVector3();
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
        Vector3 phoneY = VectorUtil.negate(getPerpendicularVector());
        Vector3 phoneX = VectorUtil.crossProduct(phoneOutwards,phoneY);

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

        float r = (float) Math.sqrt(arrowX*arrowX + arrowY*arrowY);
        calibrationView.setArrowX(arrowX);
        calibrationView.setArrowY(arrowY);
        calibrationView.setCircleRadius(1.0f/(error() + 0.1f)/10.0f);
    }
    @Override
    public void onTick() {
        updateUI();
        updateCalibrationView();
    }

    private void updateUI() {
        float phoneRa = model.getPointing().getLineOfSight().getRa();
        float phoneDec = model.getPointing().getLineOfSight().getDec();
        RaDec coordinatesPhone = new RaDec(phoneRa,phoneDec);
        phoneRaDec.setText("phone:\n" + coordinatesPhone.toString());

        Date date = getDate();
        if (date == null) {
            return;
        }

        RaDec coordinatesTarget = null;
        switch (planet) {
            case Moon:
                coordinatesTarget = Planet.calculateLunarGeocentricLocation(date);
                break;
            case Sun:
                coordinatesTarget = SolarPositionCalculator.getSolarPosition(date);
                break;
        }
        sunRaDec.setText("target:\n" + coordinatesTarget.toString());
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
