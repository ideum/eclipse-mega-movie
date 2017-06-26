package ideum.com.megamovie.Java;

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

import com.google.android.gms.maps.LocationSource;

import java.util.Date;

import javax.inject.Provider;

import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.OrientationController.AstronomerModel;
import ideum.com.megamovie.Java.OrientationController.AstronomerModelImpl;
import ideum.com.megamovie.Java.OrientationController.RealMagneticDeclinationCalculator;
import ideum.com.megamovie.Java.OrientationController.SensorOrientationController;
import ideum.com.megamovie.Java.Util.VectorUtil;
import ideum.com.megamovie.Java.Util.smoothers.PlainSmootherModelAdaptor;
import ideum.com.megamovie.Java.VIews.DirectionCalibrationView;
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
    private static final boolean SHOULD_USE_CURRENT_TIME = true;
    private Date targetDate;

    public CalibrateDirectionFragment() {
        // Required empty public constructor
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

    public void setTargetDate(Date date) {
        targetDate = date;
    }

    private Date getDate() {
        if (SHOULD_USE_CURRENT_TIME) {
            return new Date(System.currentTimeMillis());
        }
        return  targetDate;
    }


    private Float error() {
        if (model == null) {
            return null;
        }
        GeocentricCoordinates lineOfSightPointing = model.getPointing().getLineOfSight();
        float lineOfSightX = lineOfSightPointing.x;
        float lineOfSightY = lineOfSightPointing.y;
        float lineOfSightZ = lineOfSightPointing.z;

        RaDec sunRaDec = SolarPositionCalculator.getSolarPosition(model.getTime());
        GeocentricCoordinates sunGcc = new GeocentricCoordinates(1, 0, 0);
        sunGcc.updateFromRaDec(sunRaDec);
        float sunX = sunGcc.x;
        float sunY = sunGcc.y;
        float sunZ = sunGcc.z;

        Vector3 difference = new Vector3(sunX - lineOfSightX, sunY - lineOfSightY, sunZ - lineOfSightZ);
        Log.d("error",String.valueOf(difference.length()));
        return difference.length();
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

    private Vector3 getSunVector() {


        if (model == null) {
            return null;
        }
        Date date = getDate();
        if (date == null) {
            return null;
        }

        RaDec sunRaDec = SolarPositionCalculator.getSolarPosition(date);
        GeocentricCoordinates sunGcc = new GeocentricCoordinates(1, 0, 0);
        sunGcc.updateFromRaDec(sunRaDec);
        return sunGcc.getVector3();
    }

    private DirectionCalibrationView.ScreenVector getErrorVector() {
        Vector3 phoneOutwards = getLineOfSightVector();
        Vector3 phoneY = VectorUtil.negate(getPerpendicularVector());
        Vector3 phoneX = VectorUtil.crossProduct(phoneOutwards,phoneY);

        Vector3 errorVector3 = VectorUtil.difference(getSunVector(),phoneOutwards);

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
        //updateUI();
        updateCalibrationView();
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
