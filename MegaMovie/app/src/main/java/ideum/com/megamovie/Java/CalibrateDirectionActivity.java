package ideum.com.megamovie.Java;

import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.LocationSource;

import javax.inject.Provider;

import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.OrientationController.AstronomerModel;
import ideum.com.megamovie.Java.OrientationController.AstronomerModelImpl;
import ideum.com.megamovie.Java.OrientationController.RealMagneticDeclinationCalculator;
import ideum.com.megamovie.Java.OrientationController.SensorOrientationController;
import ideum.com.megamovie.Java.OrientationController.ZeroMagneticDeclinationCalculator;
import ideum.com.megamovie.Java.Util.smoothers.PlainSmootherModelAdaptor;
import ideum.com.megamovie.Java.provider.ephemeris.SolarPositionCalculator;
import ideum.com.megamovie.Java.units.GeocentricCoordinates;
import ideum.com.megamovie.Java.units.LatLong;
import ideum.com.megamovie.Java.units.RaDec;
import ideum.com.megamovie.R;

public class CalibrateDirectionActivity extends AppCompatActivity
implements MyTimer.MyTimerListener,
        LocationSource.OnLocationChangedListener{

    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    final AstronomerModel model = new AstronomerModelImpl(new RealMagneticDeclinationCalculator());
    private MyTimer timer;
    private GPSFragment mGPSFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_direction);

        tv1 = (TextView) findViewById(R.id.line_of_sight);
        tv2 = (TextView) findViewById(R.id.perpendicular);
        tv3 = (TextView) findViewById(R.id.ra_text_view);
        tv4 = (TextView) findViewById(R.id.dec_text_view);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        final PlainSmootherModelAdaptor psma = new PlainSmootherModelAdaptor(model,preferences);


        Provider<PlainSmootherModelAdaptor> psmap = new Provider<PlainSmootherModelAdaptor>() {
            @Override
            public PlainSmootherModelAdaptor get() {
                return psma;
            }
        };

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        SensorOrientationController sensorOrientationController = new SensorOrientationController(psmap,sensorManager,preferences);
        sensorOrientationController.setModel(model);
        sensorOrientationController.start();

        mGPSFragment = new GPSFragment();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.activate(this);

        timer = new MyTimer();
        timer.addListener(this);
        timer.startTicking();
    }





    @Override
    public void onTick() {
        updateUI();
    }

    private void updateUI() {
        tv1.setText(model.getPointing().getLineOfSight().toString());
//        tv2.setText(model.getPointing().getPerpendicular().toString());
        RaDec losRaDec = new RaDec(model.getPointing().getLineOfSight().getRa(),model.getPointing().getLineOfSight().getDec());
        tv3.setText("line of sight: "+ losRaDec.toString());
        RaDec sunRaDec = SolarPositionCalculator.getSolarPosition(model.getTime());
        GeocentricCoordinates gcc = new GeocentricCoordinates(1,0,0);
        gcc.updateFromRaDec(sunRaDec);
        tv2.setText(gcc.toString());

        tv4.setText("sun: " + sunRaDec.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        LatLong latLng = new LatLong(location.getLatitude(),location.getLongitude());
        if (model != null) {
            model.setLocation(latLng);
        }
    }


}
