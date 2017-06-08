package ideum.com.megamovie.Java.LocationAndTiming;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;


import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import ideum.com.megamovie.R;

public class MyMapFragment extends Fragment
        implements OnMapReadyCallback,
        View.OnClickListener,
        PlaceSelectionListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private LatLng currentLatLng;
    private LatLng plannedLatLng;

    // Parameters for initial camera position and zoom level
    private LatLng initialPoint = new LatLng(39.8, -102);
    private float initialZoom = 3.2f;


    public MyMapFragment() {
        // Required empty public constructor
    }

    public static MyMapFragment newInstance() {
        MyMapFragment fragment = new MyMapFragment();

        return fragment;
    }

    public void setCurrentLatLng(LatLng latLng) {
        currentLatLng = latLng;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_map, container, false);

        FragmentManager fm = getChildFragmentManager();

//        SupportMapFragment smf = (SupportMapFragment) fm.findFragmentById(R.id.support_map);

        SupportMapFragment smf = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (smf == null) {
            smf = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.myMap_fragment_holder, smf, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }

        smf.getMapAsync(this);

        FloatingActionButton myLocationButton = (FloatingActionButton) rootView.findViewById(R.id.my_location_fab);
        myLocationButton.setOnClickListener(this);

        SupportPlaceAutocompleteFragment paf = (SupportPlaceAutocompleteFragment) fm.findFragmentByTag("autocompleteFragment");
        if (paf == null) {
            paf = new SupportPlaceAutocompleteFragment();
            if (paf != null) {
                paf.setOnPlaceSelectedListener(this);
            }
            FragmentTransaction ft1 = fm.beginTransaction();
            ft1.add(R.id.auto_complete_fragment, paf, "autocompleteFragment");
            ft1.commit();
            fm.executePendingTransactions();

        }
        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        drawEclipsePath();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPoint));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(initialZoom));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                setPlannedLocation(latLng);
            }
        });

        setPlannedLocation(getPlannedLocationFromPreferences());


    }

    private void setPlannedLocationPreferenceValue(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(getString(R.string.planned_lng_key), (float) latLng.longitude);
        editor.putFloat(getString(R.string.planned_lat_key), (float) latLng.latitude);
        editor.commit();
    }

    private void setPlannedLocation(LatLng latLng) {
        plannedLatLng = latLng;
        setPlannedLocationPreferenceValue(latLng);
        refreshMarkersAndOverlay();
        drawPathToPlannedLocation();
    }

    public void moveToCurrentLocation() {
        if (currentLatLng == null || mMap == null) {
            return;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    }

    private void setCurrentLocationMarker() {
        if (currentLatLng == null || mMap == null) {
            return;
        }



        mMap.addMarker(new MarkerOptions().position(currentLatLng).anchor(0.5f,0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_dot)));
        //drawShortestPathToTotality(currentLatLng);
    }

    private void refreshMarkersAndOverlay() {
        mMap.clear();
        placeMarkerAtPlannedLocation();
        setCurrentLocationMarker();
        drawEclipsePath();
    }

    private LatLng getPlannedLocationFromPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        float lat = settings.getFloat(getString(R.string.planned_lat_key),0);
        float lng = settings.getFloat(getString(R.string.planned_lng_key),0);
        LatLng result = null;
        if (lat != 0 && lng != 0) {
            result = new LatLng(lat,lng);
        } else {
            result = null;
        }
        return result;
    }

    private void placeMarkerAtPlannedLocation() {
        if (plannedLatLng == null) {
            return;
        }
        mMap.addMarker(new MarkerOptions().position(plannedLatLng));
    }


    // called when the 'current location' button is pressed
    @Override
    public void onClick(View v) {
        moveToCurrentLocation();
        refreshMarkersAndOverlay();
        setPlannedLocation(EclipsePath.closestPointOnPathOfTotality(currentLatLng));
//        drawShortestPathToTotality(currentLatLng);

    }

    @Override
    public void onPlaceSelected(Place place) {
//        setPlannedLocationPreferenceValue(place.getLatLng());
        setPlannedLocation(place.getLatLng());
    }

    @Override
    public void onError(Status status) {
        Log.d("TAG", status.toString());
    }

    private void drawEclipsePath() {

        int fillColor = 0x55000066;
        PolygonOptions polygonOptions = new PolygonOptions().strokeColor(Color.BLACK).fillColor(fillColor).strokeWidth(2);

        int numPoints = 500;
        for (int i = 0; i <= numPoints; i++) {
            polygonOptions.add(EclipsePath.getLatLngForParameter(i * 1.0 / numPoints, EclipsePath.NORTH_BOUNDARY));
        }

        for (int i = numPoints; i >= 0; i--) {
            polygonOptions.add(EclipsePath.getLatLngForParameter(i * 1.0 / numPoints, EclipsePath.SOUTH_BOUNDARY));
        }
        mMap.addPolygon(polygonOptions);
    }

    private void drawPathToPlannedLocation() {
        if (currentLatLng == null || plannedLatLng == null) {
            return;
        }

        PolylineOptions plo = new PolylineOptions();
        plo.add(currentLatLng);
        plo.add(plannedLatLng);
        plo.geodesic(true);
        plo.width(5);
        plo.color(getResources().getColor(R.color.intro_color_3));
        mMap.addPolyline(plo);

    }


    private void drawShortestPathToTotality(LatLng point) {
        LatLng endpoint = EclipsePath.closestPointOnPathOfTotality(point);

        PolylineOptions plo = new PolylineOptions();
        plo.add(point);
        plo.add(endpoint);
        plo.geodesic(true);
        plo.width(5);
        plo.color(Color.RED);
        mMap.addPolyline(plo);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
       // marker.remove();
        return true;
    }
}
