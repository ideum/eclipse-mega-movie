package ideum.com.megamovie.Java.LocationAndTiming;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import ideum.com.megamovie.R;

public class MyMapFragment extends Fragment
        implements OnMapReadyCallback,
        View.OnClickListener,
        PlaceSelectionListener,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationSource mLocationSource;

    private LatLng initialPoint = new LatLng(39.8, -102);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mGoogleApiClient = new GoogleApiClient
//                .Builder(getActivity())
//                .addApi(Places.GEO_DATA_API)
//                .enableAutoManage(getActivity(), this)
//                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_map, container, false);

        SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.support_map);
        smf.getMapAsync(this);

        FloatingActionButton myLocationButton = (FloatingActionButton) rootView.findViewById(R.id.my_location_fab);
        myLocationButton.setOnClickListener(this);

        PlaceAutocompleteFragment paf = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        if (paf != null) {
            paf.setOnPlaceSelectedListener(this);
            paf.setHint("Your eclipse location?");
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PlaceAutocompleteFragment paf = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        if (paf != null) {
            getActivity().getFragmentManager().beginTransaction().remove(paf).commit();
        }

        SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.support_map);
        if (smf != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(smf).commit();
        }
    }

    public void setLocationSource(LocationSource locationSource) {
        mLocationSource = locationSource;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


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
        LatLng loc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    private void setMarker() {
        if (mLocation == null || mMap == null) {
            return;
        }
        LatLng loc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_dot)));
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


    @Override
    public void onPlaceSelected(Place place) {
        mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
    }

    @Override
    public void onError(Status status) {
        Log.d("TAG", status.toString());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
