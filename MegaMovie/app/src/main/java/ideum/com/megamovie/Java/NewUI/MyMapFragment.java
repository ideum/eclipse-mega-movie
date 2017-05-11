package ideum.com.megamovie.Java.NewUI;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import ideum.com.megamovie.Java.Utility.EclipsePath;
import ideum.com.megamovie.R;

public class MyMapFragment extends Fragment
        implements OnMapReadyCallback,
        View.OnClickListener{

    private GoogleMap mMap;
    private Location mLocation;

    private LatLng initialPoint = new LatLng(39.8,-102);
    private float initialZoom = 3.2f;

    public MyMapFragment() {
        // Required empty public constructor
    }

    public static MyMapFragment newInstance() {
        MyMapFragment fragment = new MyMapFragment();

        return fragment;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_map, container, false);

        SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.support_map);
        smf.getMapAsync(this);

        FloatingActionButton myLocationButton = (FloatingActionButton) rootView.findViewById(R.id.my_location_fab);
        myLocationButton.setOnClickListener(this);

        return rootView;
    }

    private double eclipseLatitudeCurveNorth(double lng) {
        return -57.1030 - 1.84427*lng - 0.00993789*lng*lng - .0000138865*lng*lng*lng;
    }

    private double eclipseLatitudeCurveSouth(double lng) {
        return -56.5932 - 1.77885*lng - 0.00918829*lng*lng - .0000113559*lng*lng*lng;
    }

    private LatLng eclipsePathNorth(double t) {
        double lng = -126.0 + (126.0 - 74.0) * t;
        double lat = eclipseLatitudeCurveNorth(lng);
        return new LatLng(lat,lng);
    }

    private LatLng eclipsePathSouth(double t) {
        double lng = -126.0 + (126.0 - 74.0) * t;
        double lat = eclipseLatitudeCurveSouth(lng);
        return new LatLng(lat,lng);
    }

    private void drawEclipsePath() {

        int fillColor = 0x55000066;

        PolygonOptions polygonOptions = new PolygonOptions().strokeColor(Color.BLACK).fillColor(fillColor).strokeWidth(2);

        int numPoints = 500;
        for(int i = 0;i <= numPoints; i++) {
            polygonOptions.add(EclipsePath.getLatLngForParameter(i * 1.0/numPoints,EclipsePath.NORTH_BOUNDARY));
        }

        for(int i = numPoints;i >= 0; i--) {
            polygonOptions.add(EclipsePath.getLatLngForParameter(i * 1.0/numPoints,EclipsePath.SOUTH_BOUNDARY));
        }
        mMap.addPolygon(polygonOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        setMarker();
//        moveToCurrentLocation();
        drawEclipsePath();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPoint));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(initialZoom));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                drawShortestPathToTotality(latLng);
            }
        });

    }

    private void drawShortestPathToTotality(LatLng point) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(point));
        LatLng endpoint = EclipsePath.closestPointOnPathOfTotality(point);

        PolylineOptions plo = new PolylineOptions();
        plo.add(point);
        plo.add(endpoint);
        plo.geodesic(true);
        plo.width(5);
        plo.color(Color.RED);
        mMap.addPolyline(plo);
        drawEclipsePath();
    }

    public void moveToCurrentLocation() {
        if (mLocation == null || mMap == null) {
            return;
        }
        LatLng loc = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    private void setMarker() {
        if (mLocation == null || mMap == null) {
            return;
        }
        LatLng loc = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(loc));
        drawEclipsePath();


        LatLng endpoint = EclipsePath.closestPointOnPathOfTotality(loc);

        PolylineOptions plo = new PolylineOptions();
        plo.add(loc);
        plo.add(endpoint);
        plo.geodesic(true);
        plo.width(5);
        plo.color(Color.RED);
        mMap.addPolyline(plo);
    }

    @Override
    public void onClick(View v) {
        moveToCurrentLocation();
        setMarker();
    }
}
